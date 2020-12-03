package com.csc301.songmicroservice;

import java.io.IOException;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import com.csc301.songmicroservice.DbQueryExecResult;
import com.csc301.songmicroservice.DbQueryStatus;
import com.mongodb.client.result.DeleteResult;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


@Repository
public class SongDalImpl implements SongDal {

	private final MongoTemplate db;

	@Autowired
	public SongDalImpl(MongoTemplate mongoTemplate) {
		this.db = mongoTemplate;
	}

	@Override
	public DbQueryStatus addSong(Song songToAdd) {
		try {
		  DbQueryStatus queryStatus = new DbQueryStatus("",DbQueryExecResult.QUERY_OK);
		  Song song = db.insert(songToAdd);
		  queryStatus.setData(song.getJsonRepresentation());
		  return queryStatus;
		}catch(Exception e) {
		  return new DbQueryStatus(e.getMessage(),DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
	}

	@Override
	public DbQueryStatus findSongById(String songId) {
	
	  try {
	       
        Song song = db.findById(new ObjectId(songId), Song.class);
        DbQueryStatus queryStatus = new DbQueryStatus("",DbQueryExecResult.QUERY_OK);
        queryStatus.setData(song.getJsonRepresentation());
        return queryStatus;
      }catch(Exception e) {
        return new DbQueryStatus(e.getMessage(),DbQueryExecResult.QUERY_ERROR_GENERIC);
      }
	}

	@Override
	public DbQueryStatus getSongTitleById(String songId) {
	  try {
	   
	      Song song = db.findById(new ObjectId(songId), Song.class);
	      DbQueryStatus queryStatus = new DbQueryStatus("",DbQueryExecResult.QUERY_OK);
	      queryStatus.setData(song.getSongName());
	      return queryStatus;
	    }catch(Exception e) {
	      return new DbQueryStatus(e.getMessage(),DbQueryExecResult.QUERY_ERROR_GENERIC);
	    }
	     
		
	}

	@Override
	public DbQueryStatus deleteSongById(String songId) {
	  try {
	       
        Song song = db.findById(new ObjectId(songId), Song.class);
        DbQueryStatus queryStatus = new DbQueryStatus("",DbQueryExecResult.QUERY_OK);
        db.remove(song);
        if(okhttpHelperDeleteSongFromProfile(songId)) {
          return queryStatus;
        }
        else {
          return new DbQueryStatus("deleted from mongodb but failed on neo4j",DbQueryExecResult.QUERY_ERROR_GENERIC);
        }
      }catch(Exception e) {
        return new DbQueryStatus(e.getMessage(),DbQueryExecResult.QUERY_ERROR_GENERIC);
      }
       
		
	}

	@Override
	public DbQueryStatus updateSongFavouritesCount(String songId, boolean shouldDecrement) {
	  try {
	    Song song = db.findById(new ObjectId(songId), Song.class);
        long favCount = song.getSongAmountFavourites();
        if(shouldDecrement) {
          if(favCount == 0) {
            return new DbQueryStatus("favCount is already 0 ",DbQueryExecResult.QUERY_ERROR_NOT_FOUND); 
          }
          favCount--;
        }else {
          favCount++;
        }
        song.setSongAmountFavourites(favCount);
        db.save(song);
        
        return new DbQueryStatus("",DbQueryExecResult.QUERY_OK);
      }catch(Exception e) {
        return new DbQueryStatus(e.getMessage(),DbQueryExecResult.QUERY_ERROR_GENERIC);
      }
		
	}
	private boolean okhttpHelperDeleteSongFromProfile(String songId) throws IOException {
	  OkHttpClient client = new OkHttpClient(); 
      RequestBody formBody = new FormBody.Builder()
            .add("yess", ":OOOOOO")
            .build();
      String url = String.format("http://localhost:3002/deleteAllSongsFromDb/%s", songId);
      Request request = new Request.Builder()
      .url(url).put(formBody)
      .build();
  try (Response response = client.newCall(request).execute()) {
    int responseCode = response.code();
    return responseCode == 200;
  }
	}
}