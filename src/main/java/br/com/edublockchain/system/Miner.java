package br.com.edublockchain.system;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.edublockchain.model.Block;
import br.com.edublockchain.model.Blockchain;
import br.com.edublockchain.system.communication.RabbitMQUtils;

public class Miner extends Thread{
	
	private String minerId;
	private ProofOfWork pow;
	private Blockchain blockchain;
	
	static Logger logger = LoggerFactory.getLogger(Miner.class);
	
	public Miner(String id) {
		this.minerId = id;
		this.blockchain = new Blockchain();
		this.pow = new ProofOfWork(id, this.blockchain);
	}
	
	public void receivedNewBlock(Block minedBlock) {
		logger.info("["+minerId+"] Just received a block from miner "+minedBlock.getCreatorId());
		logger.debug("["+minerId+"] Received block: "+minedBlock);
        if(ProofOfWork.isBlockValid(minedBlock)) {        	
        	//blockchain is empty: puzzle is enough
        	//blockchain is not empty: hash of last block included in mined block must be
        	//equal to hash of current last block of the blockchain
            if(blockchain.getInitialBlock()==null ||
            		minedBlock.getHashPreviousBlock().equals(Block.hashOfBlock(blockchain.getLastBlock()))) {
            	logger.info("["+minerId+"] The block just received from miner "+minedBlock.getCreatorId()+" is valid");
            	pow.setThirdPartyBlock(minedBlock);
            }
        }
	}
	
	@Override
	public void run() {
		
		RabbitMQUtils.listen(this);
		
		Thread powThread = new Thread(this.pow);
		powThread.start();
	}
	
	public String getMinerId() {
		return minerId;
	}
	
	public static void main(String[] args) {
		PropertyConfigurator.configure("src/main/resources/log4j.properties");
				
		Miner m1 = new Miner("Edu");
		Miner m2 = new Miner("Jose");
		m1.start();
		m2.start();
	}

}