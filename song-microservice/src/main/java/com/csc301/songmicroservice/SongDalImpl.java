package com.csc301.songmicroservice;

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
	  
       
		return null;
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
}