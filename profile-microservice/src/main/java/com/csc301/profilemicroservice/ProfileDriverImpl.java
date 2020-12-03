package com.csc301.profilemicroservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import org.springframework.stereotype.Repository;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.neo4j.driver.v1.Transaction;

@Repository
public class ProfileDriverImpl implements ProfileDriver {
    
	Driver driver = ProfileMicroserviceApplication.driver;

	public static void InitProfileDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.userName)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.password)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT nProfile.userName IS UNIQUE";
				trans.run(queryStr);
				
				trans.success();
			}
			session.close();
		}
	}
	
	@Override
	public DbQueryStatus createUserProfile(String userName, String fullName, String password) {
	  
	  
	  String queryStr= String.format("CREATE (a:profile{userName:\"%s\",fullName:\"%s\",password:\"%s\"})-[r:created]->(c:playlist{plName:\"%s-favorites\"})", userName,fullName,password,userName);

	  try(Session session = driver.session()){
	    try{
	       session.run(queryStr);
	    }catch(Exception e) {
	      return new DbQueryStatus(e.getMessage(),DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
	    }
	    
	    
	    session.close();
	    return new DbQueryStatus("",DbQueryExecResult.QUERY_OK);
	    
	  }catch(Exception e) {
	    return new DbQueryStatus(e.getMessage(),DbQueryExecResult.QUERY_ERROR_GENERIC);
	  }
	  
	  
	  
	   
	}

	@Override
	public DbQueryStatus followFriend(String userName, String frndUserName) {
	  String checkIfExist = String.format("MATCH (a:profile)-[r:follows]->(b:profile)WHERE a.userName =\"%s\" AND b.userName = \"%s\" return count(r) AS num", userName,frndUserName);
	  String queryStr= String.format("MATCH (a:profile),(b:profile)WHERE a.userName =\"%s\" AND b.userName = \"%s\" CREATE (a)-[:follows]->(b)", userName,frndUserName);
      try(Session session = driver.session()){
        try(Transaction tx = session.beginTransaction()){
          if(tx.run(checkIfExist).single().get("num").asInt()<1) {
            tx.run(queryStr);
            tx.success();
          }else {
            tx.success();
            session.close();
            return new DbQueryStatus("already followed",DbQueryExecResult.QUERY_OK);
          }
          
        }catch(Exception e) {
          return new DbQueryStatus(e.getMessage(),DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
        }
        
        
        session.close();
        return new DbQueryStatus("followed",DbQueryExecResult.QUERY_OK);
        
      }catch(Exception e) {
        return new DbQueryStatus(e.getMessage(),DbQueryExecResult.QUERY_ERROR_GENERIC);
      }
		
	}

	@Override
	public DbQueryStatus unfollowFriend(String userName, String frndUserName) {
	  String queryStr= String.format("MATCH (a)-[r:follows]-(b) WHERE a.userName=\"%s\" AND b.userName = \"%s\" DELETE r", userName,frndUserName);
      try(Session session = driver.session()){
        try{
           session.run(queryStr);
        }catch(Exception e) {
          return new DbQueryStatus(e.getMessage(),DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
        }
        
        
        session.close();
        return new DbQueryStatus("",DbQueryExecResult.QUERY_OK);
        
      }catch(Exception e) {
        return new DbQueryStatus(e.getMessage(),DbQueryExecResult.QUERY_ERROR_GENERIC);
      }
		
	}

	@Override
	public DbQueryStatus getAllSongFriendsLike(String userName) {
		Map<String,ArrayList<String>> dataSongTitle = new HashMap<String,ArrayList<String>>();
		String queryFollows = String.format("MATCH (a:profile)-[:follows]->(b:profile) WHERE a.userName = \"%s\" RETURN b.userName AS name", userName);
		try(Session session = driver.session()){
	        try(Transaction tx = session.beginTransaction()){
	          StatementResult follows = tx.run(queryFollows);
	          
	          follows.forEachRemaining(n->{
	            String curName = n.get("name").asString();
	            ArrayList<String> titles = new ArrayList<String>();
	            StatementResult songIds = tx.run(String.format("MATCH (b:profile)-[:created]->(p:playlist)-[:includes]->(s:song) WHERE b.userName = \"%s\" RETURN s.songId as sId", curName));
	            
	            songIds.forEachRemaining(s->{
	              try {
                  titles.add(helperGetSongTitle(s.get("sId").asString()));
                } catch (ParseException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                } catch (IOException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
                }
	            });
	            dataSongTitle.put(curName, titles);
	            
	          });
	          tx.success();
	          
	        }catch(Exception e) {
	          return new DbQueryStatus(e.getMessage(),DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
	        }
	        
	        
	        session.close();
	        DbQueryStatus queryStatus = new DbQueryStatus("",DbQueryExecResult.QUERY_OK);
            queryStatus.setData(dataSongTitle);
	        return queryStatus;
	        
	      }catch(Exception e) {
	        return new DbQueryStatus(e.getMessage(),DbQueryExecResult.QUERY_ERROR_GENERIC);
	      }
		
		
		
	}
	private String helperGetSongTitle(String songId) throws ParseException, IOException{

	  OkHttpClient client = new OkHttpClient(); 
	  String url = String.format("http://localhost:3001/getSongTitleById/%s", songId);
	  Request request = new Request.Builder()
      .url(url)
      .build();
  try (Response response = client.newCall(request).execute()) {
    String resStr = response.body().string();    
    JSONObject json = new JSONObject(resStr);
    String songtTitle = (String) json.get("data");
    return songtTitle;
  }
  

	  
	}
  


}
