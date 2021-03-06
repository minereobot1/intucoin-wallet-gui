import { Component, ViewChild, OnInit, Input } from '@angular/core';
import { Router } from "@angular/router";
import { UserState } from '../../../../user.state';
import { ModalDirective } from 'ngx-bootstrap';
import { NotificationsService } from 'angular2-notifications';

import { WalletService } from '../../../walletService.service';

import * as spescoinModels from '../../../../services/com.speseyond.api.spescoin/model/models';
import { WalletApi } from '../../../../services/com.speseyond.api.spescoin/api/WalletApi';
import { FaucetApi } from '../../../../services/com.speseyond.api.spescoin/api/FaucetApi';

import { websiteId } from '../../../../environment';
import { baseUrl } from '../../../../environment';

import 'rxjs/Rx';

@Component({
    selector: 'faucet-user',
    styleUrls: ['./overview.scss'],
    templateUrl: './overview.html'
})

export class FaucetUserOverview implements OnInit {

    @ViewChild('createAddressModal') createAddressModal: ModalDirective;

    public creatingWallet = false;
    public creatingPayment = false;

    public selectedCoin = {};

    public faucetUserAddress: spescoinModels.UserAddress = {};
    public hasFaucetUserAddress: boolean = false;

    constructor (private userState: UserState,
                 private walletApi: WalletApi,
                 private faucetApi: FaucetApi,
                 private walletService: WalletService,
                 private notificationsService: NotificationsService,
                 private router: Router) {
        this.walletApi.defaultHeaders = userState.getExtraHeaders();

        this.walletService.addressFetchedEmitter
            .subscribe(item => this.initialize(item));

    }

    public ngOnInit(): void {
        if (this.walletService.primaryCoin != undefined) {
            this.initialize(this.walletService.primaryCoin);
        } else {
            this.walletService.addressFetchedEmitter
                .subscribe(item => this.initialize(item));
        }
    }

    private initialize(coin): void {
        console.log("Initialize faucetUserAddress !!!", coin);
        let request: spescoinModels.FaucetAddressRequest = {};
        request.faucetUser = true;
        request.coin = coin;

        this.faucetApi.getFaucetAddress(true, request).subscribe(
            result => {
                console.log("faucet user address", result);
                if (result && result.address) {
                    this.faucetUserAddress = result;
                    this.hasFaucetUserAddress = true;
                }
            },
            error => {
                if (error.status === 401) {
                    this.creatingWallet = false;
                    this.userState.handleError(error, this.initialize, this);
                }
            }
        );
    }

    public getConvertedAmount() {
        if (this.faucetUserAddress && this.faucetUserAddress.balance) {
            return this.walletService.getConvertedAmount(this.walletService.primaryCoin, this.faucetUserAddress.balance.availableBalance);
        }
    }

    public getConvertedUserAmount() {
        if (this.faucetUserAddress && this.faucetUserAddress.faucetPaidBalance) {
            return this.walletService.getConvertedAmount(this.walletService.primaryCoin, this.faucetUserAddress.faucetPaidBalance.availableBalance);
        }
    }

    public createNewFaucetAddress(faucetUser: boolean) {
        if (!this.creatingWallet) {
            this.creatingWallet = true;

            let userAddress: spescoinModels.UserAddress = {};
            userAddress.currency = this.walletService.primaryCoin;

            let request: spescoinModels.CreateFaucetAddressRequest = {
                websiteId: websiteId,
                endpointRoot: baseUrl + "/spescoin/api/faucet",
                userAddress: userAddress,
                faucetUser: faucetUser
            };

            this.faucetApi.createFaucetAddress(request).subscribe(
                    result => {
                        this.creatingWallet = false;
                        if (faucetUser) {
                            this.faucetUserAddress = result;
                            this.hasFaucetUserAddress = true;
                        } else {
                            this.walletService.faucetAddress = result;
                            this.walletService.hasFaucetAddress = true;
                        }
                },
                    error => {
                    if (error.status === 401) {
                        this.creatingWallet = false;
                        this.userState.handleError(error, this.createNewFaucetAddress, this);
                    }
                }
            );
        }
    }

    public payout(address: spescoinModels.UserAddress) {
        this.creatingPayment = true;
        this.faucetApi.payout(address).subscribe(
            result => {
                if (result.ok) {
                    this.initialize(this.walletService.primaryCoin);
                    if (result.message) {
                        this.notificationsService.alert("Payment", result.message);
                    }
                    this.creatingPayment = false;
                }
            },
            error => {
                if (error.status === 401) {
                    this.creatingPayment = false;
                    this.userState.handleError(error, this.payout, this);
                }
                if (error.status === 999) {
                    this.creatingPayment = false;
                    let data = JSON.parse(error._body);
                    console.log("Catch error 999", data);
                    this.notificationsService.error("Payment failed", data.message);
                }
            }
        );
    }

    public getParsedAmount(amount: number): string {
        if (amount != undefined && amount != null) {
            return (amount / this.walletService.primaryCoin.convertAmount).toFixed(this.walletService.primaryCoin.fractionDigits) + " " + this.walletService.primaryCoin.name;
        } else {
            return (0 / this.walletService.primaryCoin.convertAmount).toFixed(this.walletService.primaryCoin.fractionDigits) + " " + this.walletService.primaryCoin.name;
        }
    }

    public copyToClipboard(value, text) {
        console.log('copyToClipboard', value, text);
        let selBox = document.createElement('textarea');

        selBox.style.position = 'fixed';
        selBox.style.left = '0';
        selBox.style.top = '0';
        selBox.style.opacity = '0';
        selBox.value = value;

        document.body.appendChild(selBox);
        selBox.focus();
        selBox.select();

        document.execCommand('copy');
        document.body.removeChild(selBox);

        console.log("Trigger notificationService");
        this.notificationsService.success("Copy", text);
    }


}
