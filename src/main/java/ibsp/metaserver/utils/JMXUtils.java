package ibsp.metaserver.utils;

import ibsp.metaserver.bean.InstanceDtlBean;
import ibsp.metaserver.global.MetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import javax.management.remote.*;
import java.io.IOException;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.*;

public class JMXUtils {
    private static volatile JMXUtils instance;
    private static Logger logger = LoggerFactory.getLogger(JMXUtils.class);
    private static final int DEFAULT_PORT = Registry.REGISTRY_PORT;
    //private static final String MBEAN_PROXY_NAME = "ibsp.metaserver.cache.access:name=Proxy";
    private static final String MBEAN_PROXY_NAME = "com.ctg.itrdc.cache.access:type=Proxy";


    private ObjectName proxyObjectName = null;
    private Map<String, JMXConnector> jmxConns;

    private JMXUtils() throws MalformedObjectNameException {
        jmxConns = new ConcurrentHashMap<>();
        proxyObjectName = new ObjectName(MBEAN_PROXY_NAME);
    };

    public static JMXUtils get() {
        if(instance == null){
            synchronized (JMXUtils.class) {
                if(instance == null) {
                    try {
                        instance = new JMXUtils();
                    } catch (MalformedObjectNameException e) {
                        logger.error("初始化JMXUtils失败! " + e.getMessage(), e);
                        e.printStackTrace();
                    }
                }
            }
        }

        return instance;
    }

    private JMXConnector initJmxConnector(String ip, String port) {
        final String urlPath = String.format("service:jmx:rmi:///jndi/rmi://%s:%s/jmxrmi",ip,
                HttpUtils.isNull(port) ? DEFAULT_PORT : port);
        Map<String, String[]> env= new HashMap<>();
        env.put(JMXConnector.CREDENTIALS, new String[]{"admin", "admin"});
        JMXConnector connector = null;
        try {
            JMXServiceURL jmxServiceURL = new JMXServiceURL(urlPath);
            connector = JMXConnectorFactory.connect(jmxServiceURL, env);
            return connector;
        } catch (Exception e) {
            if(connector != null) {
                try {
                    connector.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public Object[] getAtrributes(String proxyId, String... params) {
        JMXConnector connector = jmxConns.get(proxyId);

        if(connector == null) {
            synchronized (JMXUtils.class) {
                if(connector == null) {
                    InstanceDtlBean cacheProxy = MetaData.get().getInstanceDtlBean(proxyId);
                    String ip = cacheProxy.getAttribute(FixHeader.HEADER_IP).getAttrValue();
                    String port = cacheProxy.getAttribute(FixHeader.HEADER_STAT_PORT).getAttrValue();

                    JMXConnector connector1 = initJmxConnector(ip, port);

                    if(connector1 != null) {
                        jmxConns.put(proxyId, connector1);
                        connector = connector1;
                    }else {
                        return null;
                    }
                }
            }
        }

        MBeanServerConnection connection = null;
        try {
            connection = connector.getMBeanServerConnection();
            Object[] res = new Object[params.length];
            int i = 0 ;
            for(String param : params) {
                res[i++] = connection.getAttribute(proxyObjectName, param);
            }
            return res;
        } catch (IOException e) {
            jmxConns.remove(proxyId);
            logger.error(e.getMessage(), e);
        } catch (AttributeNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (InstanceNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (ReflectionException e) {
            logger.error(e.getMessage(), e);
        } catch (MBeanException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /*public Object getAtrribute(String ip, String port, String param) {

        JMXConnector connector1 = initJmxConnector(ip, port);
        if(connector1 != null) {
            MBeanServerConnection connection = null;
            try {
                connection = connector1.getMBeanServerConnection();
                return connection.getAttribute(proxyObjectName, param);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }*/

    public void invokeMethod(String proxyId, String method, String... params) {
        JMXConnector connector = jmxConns.get(proxyId);

        if(connector == null) {
            synchronized (JMXUtils.class) {
                if(connector == null) {
                    try{
                        InstanceDtlBean cacheProxy = MetaData.get().getInstanceDtlBean(proxyId);
                        String ip = cacheProxy.getAttribute(FixHeader.HEADER_IP).getAttrValue();
                        String port = cacheProxy.getAttribute(FixHeader.HEADER_STAT_PORT).getAttrValue();

                        JMXConnector connector1 = initJmxConnector(ip, port);

                        if(connector1 != null) {
                            jmxConns.put(proxyId, connector1);
                            connector = connector1;
                        }
                    }finally {
                        if(connector != null) {
                            try {
                                connector.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        MBeanServerConnection connection = null;
        try {
            connection = connector.getMBeanServerConnection();
            connection.invoke(proxyObjectName, method,params, null);
        } catch (IOException e) {
            jmxConns.remove(proxyId);
            logger.error(e.getMessage(), e);
        } catch (InstanceNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (ReflectionException e) {
            logger.error(e.getMessage(), e);
        } catch (MBeanException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /*public Object invokeMethod(String ip, String port, String method, String... params) {

        JMXConnector connector = initJmxConnector(ip, port);
        MBeanServerConnection connection = null;

        try {
            connection = connector.getMBeanServerConnection();
            return connection.invoke(proxyObjectName, method,params,null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }*/


    /*public static void main(String[] args) {
        System.out.println(JMXUtils.get().getAtrribute("172.20.0.82", "9301", "access_client_conns"));
        System.out.println(JMXUtils.get().invokeMethod("172.20.0.82", "9301", "execute",
                new String[]{"dsd"}));
    }*/

}
