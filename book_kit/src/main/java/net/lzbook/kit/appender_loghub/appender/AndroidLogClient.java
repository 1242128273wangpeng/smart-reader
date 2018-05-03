package net.lzbook.kit.appender_loghub.appender;


import net.lzbook.kit.appender_loghub.LOGClient;
import net.lzbook.kit.appender_loghub.LogGroup;
import net.lzbook.kit.appender_loghub.ServerLog;
import net.lzbook.kit.utils.AppLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class AndroidLogClient {

    //华南 大数据
    public static final String endPoint = "cn-shenzhen.log.aliyuncs.com";// Endpoint
    public static final String accessKeyId = "LTAIHv56dMm1Dd5Z"; // 使用您的阿里云访问密钥
    public static final String accessKeySecret = "30hIE7U1i6D4azaCwsWnFWS19G4yAb"; // 使用您的阿里云访问密钥

    //广告的阿里云相关
    public static final String adEndPoint = "cn-shanghai.log.aliyuncs.com";// Endpoint
    public static final String adAccessKeyId = "LTAIHv56dMm1Dd5Z"; // 使用您的阿里云访问密钥
    public static final String adAccessKeySecret = "30hIE7U1i6D4azaCwsWnFWS19G4yAb"; // 使用您的阿里云访问密钥

//    "cn-shanghai.log.aliyuncs.com", "LTAIHv56dMm1Dd5Z", "30hIE7U1i6D4azaCwsWnFWS19G4yAb","basestatistics"

    //	private final static Logger logger = LoggerFactory.getLogger(AndroidLogClient.class);
    // 定义发送队列
    private static volatile Queue<LogGroup> send_queue = new ConcurrentLinkedQueue<LogGroup>();
    private static LogSendThread senderThread;

    static {
        senderThread = new LogSendThread();
        senderThread.start();
    }

	/*public static void putLog(List<Map<String,Object>> list) {
        try {
			//按照project和logstore分组，所以log中必须包含project和logstore两个key
			Map<String, LogGroup> map = new HashMap<>();
			if (log != null && log.length > 0) {
				for(int i=0 ; i< log.length ; i++){
					if(!log[i].GetContent().containsKey("project") || !log[i].GetContent().containsKey("logstore")){
						logger.error("log not containsKey : project || logstore");
						continue;
					}
					String project = (String) log[i].GetContent().get("project");
					String logstore = (String) log[i].GetContent().get("logstore");
					String unikey = project+"_"+logstore;
					if(!map.containsKey(unikey)){
						LogGroup logGroup = new LogGroup("", "",project,logstore);
						map.put(unikey, logGroup);
					}
					map.get(unikey).PutLog(log[i]);
				}
				if(!map.values().isEmpty()){
					for(LogGroup logGroup : map.values()){
						send_queue.add(logGroup);
						senderThread.wakeUp();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("put element to queue error ", e);
		}
	}


	public static void putLog(Map<String,Object> map) {
		try {
			if(!map.containsKey("project") || ! map.containsKey("logstore")){
				logger.error("log not containsKey : project || logstore");
				return;
			}
			Log log = new Log();
			for(Entry<String, Object> entry : map.entrySet()){

			}


			//按照project和logstore分组，所以log中必须包含project和logstore两个key
			Map<String, LogGroup> map = new HashMap<>();
			if (log != null && log.length > 0) {
				for(int i=0 ; i< log.length ; i++){
					if(!log[i].GetContent().containsKey("project") || !log[i].GetContent().containsKey("logstore")){
						logger.error("log not containsKey : project || logstore");
						continue;
					}
					String project = (String) log[i].GetContent().get("project");
					String logstore = (String) log[i].GetContent().get("logstore");
					String unikey = project+"_"+logstore;
					if(!map.containsKey(unikey)){
						LogGroup logGroup = new LogGroup("", "",project,logstore);
						map.put(unikey, logGroup);
					}
					map.get(unikey).PutLog(log[i]);
				}
				if(!map.values().isEmpty()){
					for(LogGroup logGroup : map.values()){
						send_queue.add(logGroup);
						senderThread.wakeUp();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("put element to queue error ", e);
		}
	}*/

    public static void putLog(List<ServerLog> logList) {
        try {
            //按照project和logstore分组，所以log中必须包含project和logstore两个key
            Map<String, LogGroup> map = new HashMap<>();
            if (logList != null && logList.size() > 0) {
                for (int i = 0; i < logList.size(); i++) {
                    if (!logList.get(i).GetContent().containsKey("project") || !logList.get(i).GetContent().containsKey("logstore")) {
//						loggerer.error("log not containsKey : project || logstore");
                        continue;
                    }
                    String project = (String) logList.get(i).GetContent().get("project");
                    String logstore = (String) logList.get(i).GetContent().get("logstore");
                    String unikey = project + "_" + logstore;
                    if (!map.containsKey(unikey)) {
                        LogGroup logGroup = new LogGroup("", "", project, logstore);
                        map.put(unikey, logGroup);
                    }
                    map.get(unikey).PutLog(logList.get(i));
                    AppLog.e("ad", (String) logList.get(i).GetContent().get("project"));
                }
                if (!map.values().isEmpty()) {
                    for (LogGroup logGroup : map.values()) {
                        send_queue.add(logGroup);
                        senderThread.wakeUp();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
//			logger.error("put element to queue error ", e);
        }
    }

    // 开启一个线程专门用来发送数据
    private static class LogSendThread extends Thread {
        private volatile boolean loop = true;
        private volatile boolean is_sleeping = false;

        public LogSendThread() {
            super();
        }

        public void run() {
            while (loop) {
                is_sleeping = false;
                if (send_queue.isEmpty()) {
                    is_sleeping = true;
                    synchronized (this) {
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                        }
                    }
                    continue;
                }
                LogGroup logGroup = send_queue.poll();
                String project = logGroup.getProject();
                String logstore = logGroup.getLogstore();
                try {
                    if (logGroup != null && logGroup != null) {
                        LOGClient logClient = new LOGClient(endPoint, accessKeyId, accessKeySecret, project);
                        logClient.PostLog(logGroup, logstore);
                    }
                    // 是否要有重试功能.......
                } catch (Exception e) {
                    e.printStackTrace();
//						logger.error("send log error", e);
                    AndroidLogStorage.getInstance().consumeFail(logGroup.getLogs());
                }
            }
        }

        @SuppressWarnings("unused")
        public void stopThred() {
            loop = false;
            synchronized (this) {
                this.notify();
            }
        }

        @SuppressWarnings("unused")
        public boolean isSleeping() {
            return is_sleeping;
        }

        public void wakeUp() {
            synchronized (this) {
                this.notify();
            }
        }

    }

}
