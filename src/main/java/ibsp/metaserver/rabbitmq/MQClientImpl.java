package ibsp.metaserver.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;

import ibsp.metaserver.utils.CONSTS;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.AMQP.Queue;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MQClientImpl implements IMQClient {
	
	private static Logger logger = LoggerFactory.getLogger(MQClientImpl.class);

	private static int PREFETCH_COUNT = 300;
	private static boolean PREFETCH_GLOBAL = true;

	private ConnectionFactory factory;
	private Connection conn;
	private Channel dataChannel, cmdChannel;
	
	private String lastErrMsg;

	public MQClientImpl() {
		lastErrMsg = "";
		factory = null;
		dataChannel = null;
		cmdChannel = null;
	}

	@Override
	public int connect(String userName,String password,String vhost,String host,int port) {
		int ret = -1;
		try {
			factory = new ConnectionFactory();
			factory.setUsername(userName);
			factory.setPassword(password);
			factory.setVirtualHost(vhost);
			factory.setHost(host);
			factory.setPort(port);
		} catch (Exception e) {
			lastErrMsg = e.getMessage();
			logger.error(e.getMessage(), e);
			return ret;
		}

		try {
			conn = factory.newConnection();
			if (conn != null) {
				dataChannel = conn.createChannel();
				dataChannel.basicQos(PREFETCH_COUNT, PREFETCH_GLOBAL);
				cmdChannel = conn.createChannel();
			}
			ret = 0;
		} catch (Exception e) {
			lastErrMsg = e.getMessage();
			logger.error(e.getMessage(), e);
			return ret;
		}
		return ret;
	}

	@Override
	public void close() {
		if (dataChannel != null) {
			try {
				dataChannel.close();
			} catch (Exception e) {
				lastErrMsg = "close dataChannel caught exception:"+ e.getMessage();
				logger.error(e.getMessage(), e);
			}
		}
		if (cmdChannel != null) {
			try {
				cmdChannel.close();
			} catch (Exception e) {
				lastErrMsg = "close cmdChannel caught exception:"+ e.getMessage();
				logger.error(e.getMessage(), e);
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (IOException e) {
				lastErrMsg = "close Connection caught exception:"+ e.getMessage();
				logger.error(e.getMessage(), e);
			}
		}
	}

	@Override
	public int createQueue(String queueName, boolean ordinal, boolean DURABLE) {
		int ret = CONSTS.REVOKE_NOK;
		if (cmdChannel != null) {
			try {
				Queue.DeclareOk decResult = cmdChannel.queueDeclare(queueName, DURABLE, false, false, null);
				if (decResult != null && decResult.getQueue().equals(queueName)) {
					ret = CONSTS.REVOKE_OK;
				}
			} catch (IOException e) {
				lastErrMsg = e.getMessage();
				logger.error(e.getMessage(), e);
				return ret;
			}
		} else {
			lastErrMsg = "cmdChannel is null";
		}
		return ret;
	}

	@Override
	public int deleteQueue(String queueName) {
		int ret = CONSTS.REVOKE_NOK;
		if (cmdChannel != null) {
			try {
				Queue.DeleteOk delResult = cmdChannel.queueDelete(queueName);
				if (delResult != null) {
					ret = CONSTS.REVOKE_OK;
				}
			} catch (IOException e) {
				lastErrMsg = e.getMessage();
				logger.error(e.getMessage(), e);
				return ret;
			}
		} else {
			lastErrMsg = "cmdChannel is null";
		}
		return ret;
	}

	@Override
	public String GetLastErrorMessage() {
		return lastErrMsg;
	}

	@Override
	public int createTopic(String topicName, boolean ordinal, boolean DURABLE) {
		int ret = CONSTS.REVOKE_NOK;
		if (cmdChannel != null) {
			try {
				Queue.DeclareOk decResult = cmdChannel.queueDeclare(topicName, DURABLE, false, false, null);
				if (decResult != null && decResult.getQueue().equals(topicName)) {
					ret = CONSTS.REVOKE_OK;
				}
			} catch (IOException e) {
				lastErrMsg = e.getMessage();
				logger.error(e.getMessage(), e);
				return ret;
			}
		} else {
			lastErrMsg = "cmdChannel is null";
		}
		return ret;
	}

	@Override
	public int deleteTopic(String topicName) {
		int ret = CONSTS.REVOKE_NOK;
		if (cmdChannel != null) {
			try {
				Queue.DeleteOk delResult = cmdChannel.queueDelete(topicName);
				if (delResult != null) {
					ret = CONSTS.REVOKE_OK;
				}
			} catch (IOException e) {
				lastErrMsg = e.getMessage();
				logger.error(e.getMessage(), e);
				return ret;
			}
		} else {
			lastErrMsg = "cmdChannel is null";
		}
		return ret;
	}
	
	@Override
	public int queueBind(String queueName, String exchangeName, String routingKey) {
		int ret = CONSTS.REVOKE_NOK;
		if (cmdChannel != null) {
			try {
				Queue.BindOk bindResult = cmdChannel.queueBind(queueName, exchangeName, routingKey);
				if (bindResult != null) {
					ret = CONSTS.REVOKE_OK;
				}
			} catch (IOException e) {
				lastErrMsg = e.getMessage();
				logger.error(e.getMessage(), e);
				return ret;
			}
		} else {
			lastErrMsg = "cmdChannel is null";
		}
		return ret;
	}
	
	public int purgeQueue(String queueName) {
		int ret = CONSTS.REVOKE_NOK;
		
		if (cmdChannel != null) {
			try {
				Queue.PurgeOk purgeResult = cmdChannel.queuePurge(queueName);
				if (purgeResult != null) {
					ret = CONSTS.REVOKE_OK;
				}
			} catch (IOException e) {
				lastErrMsg = e.getMessage();
				logger.error(e.getMessage(), e);
				return ret;
			}
		} else {
			lastErrMsg = "cmdChannel is null";
		}
		
		return ret;
	}
}
