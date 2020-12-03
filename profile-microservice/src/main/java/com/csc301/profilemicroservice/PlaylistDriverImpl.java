package com.csc301.profilemicroservice;

import java.io.IOException;
import org.json.JSONObject;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.stereotype.Repository;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.neo4j.driver.v1.Transaction;

@Repository
public class PlaylistDriverImpl implements PlaylistDriver {

	Driver driver = ProfileMicroserviceApplication.driver;

	public static void InitPlaylistDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nPlaylist:playlist) ASSERT exists(nPlaylist.plName)";
				trans.run(queryStr);
				trans.success();
			}
			session.close();
		}
	}

	@Override
	public DbQueryStatus likeSong(String userName, String songId) {
	  String checkIfExist= String.format("MATCH (d:playlist)-[:includes]->(s:song) WHERE d.plName = \"%s-favorites\" AND s.songId = \"%s\"RETURN count(s) as num", userName,songId);
	  String createSongAndRelation = String.format("MATCH (d:playlist) WHERE d.plName = \"%s-favorites\" CREATE (d)-[:includes]->(s:song{songId:\"%s\"})", userName,songId);
      try(Session session = driver.session()){
        try(Transaction tx = session.beginTransaction()){
           StatementResult result = tx.run(checkIfExist);
           if(result.single().get("num").asInt()<1){
             tx.run(createSongAndRelation);
             tx.success();
             
           }else {
             tx.success();
             session.close();
             return new DbQueryStatus("already liked the song",DbQueryExecResult.QUERY_OK);
           }
        }catch(Exception e) {
          return new DbQueryStatus(e.getMessage(),DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
        }
        
        
        session.close();
        
        if(likeandunlike(songId,"false")) {
          return new DbQueryStatus("successfulltin liked",DbQueryExecResult.QUERY_OK);
        }
        
        return new DbQueryStatus("error request to mongodb",DbQueryExecResult.QUERY_ERROR_GENERIC);
        
      }catch(Exception e) {
        return new DbQueryStatus(e.getMessage(),DbQueryExecResult.QUERY_ERROR_GENERIC);
      }
		
	}

	@Override
	public DbQueryStatus unlikeSong(String userName, String songId) {
	  String queryStr= String.format("MATCH (a:playlist)-[r:includes]-(b:song) WHERE a.plName=\"%s-favorites\" AND b.songId = \"%s\" DETACH DELETE b", userName,songId);
      try(Session session = driver.session()){
        try{
           session.run(queryStr);
           
        }catch(Exception e) {
          return new DbQueryStatus(e.getMessage(),DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
        }
        
        
        session.close();
        if(likeandunlike(songId,"true")) {
          return new DbQueryStatus("",DbQueryExecResult.QUERY_OK);
        }
        
        return new DbQueryStatus("error request to mongodb",DbQueryExecResult.QUERY_ERROR_GENERIC);
      }catch(Exception e) {
        return new DbQueryStatus(e.getMessage(),DbQueryExecResult.QUERY_ERROR_GENERIC);
      }
		
	}

	@Override
	public DbQueryStatus deleteSongFromDb(String songId) {
		
	  String queryStr= String.format("MATCH (n:song) WHERE n.songId =\"%s\" DETACH DELETE n",songId);

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
	private boolean likeandunlike(String songId,String shouldDecrement) throws IOException {
	  OkHttpClient client = new OkHttpClient(); 
	  RequestBody formBody = new FormBody.Builder()
	        .add("yess", ":OOOOOO")
	        .build();
      String url = String.format("http://localhost:3001/updateSongFavouritesCount/%s?shouldDecrement=%s", songId,shouldDecrement);
      Request request = new Request.Builder()
      .url(url).put(formBody)
      .build();
  try (Response response = client.newCall(request).execute()) {
    int responseCode = response.code();
    return responseCode == 200;
  }
	}
}

