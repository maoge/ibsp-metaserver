package ibsp.metaserver.rabbitmq;


public interface IMQClient {

	/**
	 * 方法说明：使用指定连接串connString（格式自定义），建立客户端连接。返回0代表连接建立成功，非0失败
	 * 
	 * @param connString
	 * @return
	 */
	public int connect(String userName,String password,String vhost,String host,int port);

	/**
	 * 方法说明：关闭客户端连接
	 * 
	 */
	public void close();

	/**
	 * 方法说明：返回最后的错误信息
	 * 
	 * @return
	 */
	public String GetLastErrorMessage();

	/**
	 * 方法说明：创建名称为queueName的队列，ordinal代表是否队列中的消息是否要求顺序性，isHA表示队列是否主备。返回0代表成功，非0失败
	 * 
	 * @param queueName
	 * @param ordinal
	 * @param DURABLE 是否持久化
	 * @param nMaxPriority 优先级队列最大优先级，默认0非优先级队列
	 * @return
	 */
	public int createQueue(String queueName, boolean ordinal, boolean duarable, int nMaxPriority);

	/**
	 * 方法说明：删除名称为queueName 队列。返回0代表成功，非0失败
	 * 
	 * @param queueName
	 * @return
	 */
	public int deleteQueue(String queueName);
	
	public int createTopic(String topicName, boolean ordinal, boolean DURABLE);
	
	public int deleteTopic(String topicName);

	public int queueBind(String queueName, String exchangeName, String routingKey);
	
	public int purgeQueue(String queueName);

}
