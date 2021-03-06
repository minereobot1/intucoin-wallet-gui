import { Component, ViewChild, Input } from '@angular/core';
import { Router } from '@angular/router';
import { ModalDirective } from 'ngx-bootstrap';

import { UserState } from '../../../user.state';
import { WalletService } from '../../walletService.service';
import { TransactionsService } from '../../transactions/transactions.service';

import 'style-loader!./serverInfo.scss';

import * as spescoinModels from '../../../services/com.speseyond.api.spescoin/model/models';
import { WalletApi } from '../../../services/com.speseyond.api.spescoin/api/WalletApi';


@Component({
    selector: 'server-info',
    templateUrl: './serverInfo.html'
})
export class ServerInfo {

    @Input('coin')
    public coin : spescoinModels.WalletCoin;

    constructor (private userState: UserState,
                 private walletApi: WalletApi,
                 private walletService: WalletService,
                 private transactionsService: TransactionsService,
                 private router: Router) {
        this.walletApi.defaultHeaders = userState.getExtraHeaders();
    }

    public getNumberOfCoinsInNetwork(): string {
        if (this.walletService.serverInfos[this.coin.name]) {
            return (this.walletService.serverInfos[this.coin.name].block.alreadyGeneratedCoins / this.coin.convertAmount).toFixed(this.coin.fractionDigits) + " " + this.coin.name;
        } else {
            return "";
        }
    }

    public getCurrentBlockReward(): string {
        if (this.walletService.serverInfos[this.coin.name]) {
            return (this.walletService.serverInfos[this.coin.name].block.reward / this.coin.convertAmount).toFixed(this.coin.fractionDigits) + " " + this.coin.name;
        } else {
            return "";
        }
    }

    public getCurrentBlockHeight(): string {
        if (this.walletService.serverInfos[this.coin.name]) {
            return this.walletService.serverInfos[this.coin.name].block.height;
        } else {
            return "";
        }
    }

    public getDifficulty(): string {
        if (this.walletService.serverInfos[this.coin.name]) {
            return this.walletService.serverInfos[this.coin.name].block.difficulty;
        } else {
            return "";
        }
    }

    public getLastBlockHash (): string {
        if (this.walletService.serverInfos[this.coin.name]) {
            return this.walletService.serverInfos[this.coin.name].block.hash;
        } else {
            return "";
        }
    }

    public gotoLastBlock() {
        this.transactionsService.searchString = this.walletService.serverInfos[this.coin.name].block.hash;
        this.transactionsService.coin = this.coin;
        console.log("Starting blockchain search", this.transactionsService.searchString);
        if (this.transactionsService.searchString != undefined && this.transactionsService.searchString != "") {
            this.router.navigateByUrl("pages/explorer");
            this.transactionsService.triggerSearch();
        }
    }

}
