package com.speseyond.wallet.spescoin.controler;

import com.speseyond.wallet.rpc.JsonRpcExecutor;
import com.speseyond.wallet.rpc.RpcPoller;
import com.speseyond.wallet.rpc.model.Address;
import com.speseyond.wallet.rpc.model.AddressBalance;
import com.speseyond.wallet.rpc.model.Addresses;
import com.speseyond.wallet.rpc.model.FusionEstimate;
import com.speseyond.wallet.rpc.model.FusionTransaction;
import com.speseyond.wallet.rpc.model.Payment;
import com.speseyond.wallet.rpc.model.SingleTransactionItem;
import com.speseyond.wallet.rpc.model.SpendKeys;
import com.speseyond.wallet.rpc.model.Status;
import com.speseyond.wallet.rpc.model.Success;
import com.speseyond.wallet.rpc.model.TransactionItems;
import com.speseyond.wallet.rpc.model.UnconfirmedTransactionHashes;
import com.speseyond.wallet.rpc.model.ViewSecretKey;
import com.speseyond.wallet.rpc.WalletController;

import java.util.ArrayList;
import java.util.List;

public class WalletRpcController implements WalletController {

	private JsonRpcExecutor<ViewSecretKey> viewSecretKeyExecutor;
	private JsonRpcExecutor<SpendKeys> spendKeysExecutor;
	private JsonRpcExecutor<Status> statusExecutor;
	private JsonRpcExecutor<Address> createAddressExecutor;
	private JsonRpcExecutor<Success> deleteAddressExecutor;
	private JsonRpcExecutor<AddressBalance> balanceExecutor;
	private JsonRpcExecutor<Addresses> addressesExecutor;
	private JsonRpcExecutor<Payment> paymentExecutor;
	private JsonRpcExecutor<TransactionItems> transactionsExecutor;
	private JsonRpcExecutor<SingleTransactionItem> transactionExecutor;
	private JsonRpcExecutor<UnconfirmedTransactionHashes> unconfirmedTransactionHashesExecutor;

	private JsonRpcExecutor<FusionEstimate> fusionEstimateExecutor;
	private JsonRpcExecutor<FusionTransaction> fusionTransactionExecutor;

	private JsonRpcExecutor<Void> resetExecutor;
	private JsonRpcExecutor<Void> saveExecutor;

	private List<RpcPoller<?>> pollers = new ArrayList<>();

	public WalletRpcController(String baseUrl) {
		viewSecretKeyExecutor = new JsonRpcExecutor<>(baseUrl + "/json_rpc", "getViewKey", ViewSecretKey.class);
		spendKeysExecutor = new JsonRpcExecutor<>(baseUrl + "/json_rpc", "getSpendKeys", SpendKeys.class);
		statusExecutor = new JsonRpcExecutor<>(baseUrl + "/json_rpc", "getStatus", Status.class);
		createAddressExecutor = new JsonRpcExecutor<>(baseUrl + "/json_rpc", "createAddress", Address.class);
		deleteAddressExecutor = new JsonRpcExecutor<>(baseUrl + "/json_rpc", "deleteAddress", Success.class);
		balanceExecutor = new JsonRpcExecutor<>(baseUrl + "/json_rpc", "getBalance", AddressBalance.class);
		addressesExecutor = new JsonRpcExecutor<>(baseUrl + "/json_rpc", "getAddresses", Addresses.class);
		paymentExecutor = new JsonRpcExecutor<>(baseUrl + "/json_rpc", "sendTransaction", Payment.class);
		transactionsExecutor = new JsonRpcExecutor<>(baseUrl + "/json_rpc", "getTransactions", TransactionItems.class);
		transactionExecutor = new JsonRpcExecutor<>(baseUrl + "/json_rpc", "getTransaction", SingleTransactionItem.class);
		unconfirmedTransactionHashesExecutor = new JsonRpcExecutor<>(baseUrl + "/json_rpc", "getUnconfirmedTransactionHashes", UnconfirmedTransactionHashes.class);

		fusionEstimateExecutor = new JsonRpcExecutor<>(baseUrl + "/json_rpc", "estimateFusion", FusionEstimate.class);
		fusionTransactionExecutor = new JsonRpcExecutor<>(baseUrl + "/json_rpc", "sendFusionTransaction", FusionTransaction.class);

		resetExecutor = new JsonRpcExecutor<>(baseUrl + "/json_rpc", "reset", Void.class);
		saveExecutor = new JsonRpcExecutor<>(baseUrl + "/json_rpc", "save", Void.class);
	}

	public void addPollers(RpcPoller poller) {
		pollers.add(poller);
		new Thread(poller).start();
	}

	public void stop() {
		for (RpcPoller poller : pollers) {
			poller.stop();
		}
	}

	public void restart() {
		for (RpcPoller poller : pollers) {
			new Thread(poller).start();
		}
	}

	@Override
	public JsonRpcExecutor<ViewSecretKey> getViewSecretKeyExecutor() {
		return viewSecretKeyExecutor;
	}

	@Override
	public JsonRpcExecutor<SpendKeys> getSpendKeysExecutor() {
		return spendKeysExecutor;
	}

	@Override
	public JsonRpcExecutor<Status> getStatusExecutor() {
		return statusExecutor;
	}

	@Override
	public JsonRpcExecutor<Address> getCreateAddressExecutor() {
		return createAddressExecutor;
	}

	@Override
	public JsonRpcExecutor<Success> getDeleteAddressExecutor() {
		return deleteAddressExecutor;
	}

	@Override
	public JsonRpcExecutor<AddressBalance> getBalanceExecutor() {
		return balanceExecutor;
	}

	@Override
	public JsonRpcExecutor<Addresses> getAddressesExecutor() {
		return addressesExecutor;
	}

	@Override
	public JsonRpcExecutor<Payment> getPaymentExecutor() {
		return paymentExecutor;
	}

	@Override
	public JsonRpcExecutor<TransactionItems> getTransactionsExecutor() {
		return transactionsExecutor;
	}

	@Override
	public JsonRpcExecutor<SingleTransactionItem> getTransactionExecutor() {
		return transactionExecutor;
	}

	@Override
	public JsonRpcExecutor<UnconfirmedTransactionHashes> getUnconfirmedTransactionHashesExecutor() {
		return unconfirmedTransactionHashesExecutor;
	}

	@Override
	public JsonRpcExecutor<FusionEstimate> getFusionEstimateExecutor() {
		return fusionEstimateExecutor;
	}

	@Override
	public JsonRpcExecutor<FusionTransaction> getFusionTransactionExecutor() {
		return fusionTransactionExecutor;
	}

	@Override
	public JsonRpcExecutor<Void> getResetExecutor() {
		return resetExecutor;
	}

	@Override
	public JsonRpcExecutor<Void> getSaveExecutor() {
		return saveExecutor;
	}
}
