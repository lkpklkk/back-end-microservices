package com.csc301.profilemicroservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import org.springframework.stereotype.Repository;
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
	  String queryStr= String.format("MATCH (a:profile),(b:profile)WHERE a.userName =\"%s\" AND b.userName = \"%s\" CREATE (a)-[:follows]->(b)", userName,frndUserName);
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
			
		return null;
	}
}
