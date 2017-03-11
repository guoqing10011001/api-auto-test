package cn.simafei.test.utils;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

public class MongoDBManage {

	@SuppressWarnings({ "resource", "unused" })
	public void getConnection(){
		try {
			// 连接到 mongodb 服务
			MongoClient mongoClient = new MongoClient("172.31.160.3", 27017);
			// 连接到数据库
			MongoDatabase mongoDatabase = mongoClient
					.getDatabase("FS-OPEN-MSG");
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage() );
		}  
		 
	}
}
