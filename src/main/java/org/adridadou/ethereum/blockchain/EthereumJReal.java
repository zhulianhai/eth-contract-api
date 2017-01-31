package org.adridadou.ethereum.blockchain;

import org.adridadou.ethereum.event.EthereumEventHandler;
import org.adridadou.ethereum.provider.LocalExecutionServiceImpl;
import org.adridadou.ethereum.values.*;
import org.adridadou.ethereum.values.config.ChainId;
import org.ethereum.core.*;
import org.ethereum.facade.Ethereum;

import java.math.BigInteger;

import static org.adridadou.ethereum.values.EthValue.wei;

/**
 * Created by davidroon on 20.01.17.
 */
public class EthereumJReal implements EthereumBackend {
    private final Ethereum ethereum;
    private final LocalExecutionServiceImpl localExecutionService;

    public EthereumJReal(Ethereum ethereum, ChainId chainId) {
        this.ethereum = ethereum;
        this.localExecutionService = new LocalExecutionServiceImpl((BlockchainImpl)ethereum.getBlockchain(), chainId);
    }

    @Override
    public BigInteger getGasPrice() {
        return BigInteger.valueOf(ethereum.getGasPrice());
    }

    @Override
    public EthValue getBalance(EthAddress address) {
        return wei(getRepository().getBalance(address.address));
    }

    @Override
    public boolean addressExists(EthAddress address) {
        return getRepository().isExist(address.address);
    }

    @Override
    public EthData submit(EthAccount account, EthAddress address, EthValue value, EthData data, BigInteger nonce) {
        Transaction tx = ethereum.createTransaction(nonce, getGasPrice(), estimateGas(account, address, value, data), address.address, value.inWei(), data.data);
        tx.sign(account.key);
        ethereum.submitTransaction(tx);

        return EthData.of(tx.getHash());
    }

    @Override
    public BigInteger getNonce(EthAddress currentAddress) {
        return getRepository().getNonce(currentAddress.address);
    }

    @Override
    public long getCurrentBlockNumber() {
        return getBlockchain().getBestBlock().getNumber();
    }

    @Override
    public SmartContractByteCode getCode(EthAddress address) {
        return SmartContractByteCode.of(getRepository().getCode(address.address));
    }

    @Override
    public EthData executeLocally(EthAccount account, EthAddress address, EthValue value, EthData data) {
        return localExecutionService.executeLocally(account, address, value, data, getNonce(account.getAddress()));
    }

    public BlockchainImpl getBlockchain() {
        return (BlockchainImpl) ethereum.getBlockchain();
    }

    private Repository getRepository() {
        return getBlockchain().getRepository();
    }

    @Override
    public void addListener(EthereumEventHandler ethereumEventHandler) {
        ethereum.addListener(ethereumEventHandler);
    }

    @Override
    public BigInteger estimateGas(EthAccount account, EthAddress address, EthValue value, EthData data) {
        return localExecutionService.estimateGas(account, address, value, data, getNonce(account.getAddress()));
    }

}
