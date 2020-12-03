package com.csc301.profilemicroservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.csc301.profilemicroservice.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/")
public class ProfileController {
	public static final String KEY_USER_NAME = "userName";
	public static final String KEY_USER_FULLNAME = "fullName";
	public static final String KEY_USER_PASSWORD = "password";

	@Autowired
	private final ProfileDriverImpl profileDriver;

	@Autowired
	private final PlaylistDriverImpl playlistDriver;

	OkHttpClient client = new OkHttpClient();

	public ProfileController(ProfileDriverImpl profileDriver, PlaylistDriverImpl playlistDriver) {
		this.profileDriver = profileDriver;
		this.playlistDriver = playlistDriver;
	}

	@RequestMapping(value = "/profile", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> addProfile(@RequestParam Map<String, String> params,
			HttpServletRequest request) {
	    
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("POST %s", Utils.getUrl(request)));
		DbQueryStatus queryStatus  = profileDriver.createUserProfile(params.get(KEY_USER_NAME), params.get(KEY_USER_FULLNAME), params.get(KEY_USER_PASSWORD));
		response = Utils.setResponseStatus(response, queryStatus.getdbQueryExecResult(), queryStatus.getData());
		return response;
	}
	
	

	@RequestMapping(value = "/followFriend/{userName}/{friendUserName}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> followFriend(@PathVariable("userName") String userName,
			@PathVariable("friendUserName") String friendUserName, HttpServletRequest request) {
	    DbQueryStatus queryStatus  = profileDriver.followFriend(userName, friendUserName);
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		if(queryStatus.getdbQueryExecResult() == DbQueryExecResult.QUERY_OK) {
          response.put("status",HttpStatus.OK);
        }else {
          response.put("status",HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
		
	}

	@RequestMapping(value = "/getAllFriendFavouriteSongTitles/{userName}", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getAllFriendFavouriteSongTitles(@PathVariable("userName") String userName,
			HttpServletRequest request) {
	  DbQueryStatus queryStatus  = profileDriver.getAllSongFriendsLike(userName);
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		response = Utils.setResponseStatus(response, queryStatus.getdbQueryExecResult(), queryStatus.getData());
		return response;
	}


	@RequestMapping(value = "/unfollowFriend/{userName}/{friendUserName}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> unfollowFriend(@PathVariable("userName") String userName,
			@PathVariable("friendUserName") String friendUserName, HttpServletRequest request) {
	  DbQueryStatus queryStatus  = profileDriver.unfollowFriend(userName, friendUserName);
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		if(queryStatus.getdbQueryExecResult() == DbQueryExecResult.QUERY_OK) {
          response.put("status",HttpStatus.OK);
        }else {
          response.put("status",HttpStatus.INTERNAL_SERVER_ERROR);
        }
		return response;
	}

	@RequestMapping(value = "/likeSong/{userName}/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> likeSong(@PathVariable("userName") String userName,
			@PathVariable("songId") String songId, HttpServletRequest request) {
	  DbQueryStatus queryStatus  = playlistDriver.likeSong(userName, songId);
      Map<String, Object> response = new HashMap<String, Object>();
      response.put("path", String.format("PUT %s", Utils.getUrl(request)));
      if(queryStatus.getdbQueryExecResult() == DbQueryExecResult.QUERY_OK) {
        response.put("status",HttpStatus.OK);
      }else {
        response.put("status",HttpStatus.INTERNAL_SERVER_ERROR);
      }
      response.put("message", queryStatus.getMessage());
      return response;
	}
	
	@RequestMapping(value = "/unlikeSong/{userName}/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> unlikeSong(@PathVariable("userName") String userName,
			@PathVariable("songId") String songId, HttpServletRequest request) {

	  DbQueryStatus queryStatus  = playlistDriver.unlikeSong(userName, songId);
      Map<String, Object> response = new HashMap<String, Object>();
      response.put("path", String.format("PUT %s", Utils.getUrl(request)));
      if(queryStatus.getdbQueryExecResult() == DbQueryExecResult.QUERY_OK) {
        response.put("status",HttpStatus.OK);
      }else {
        response.put("status",HttpStatus.INTERNAL_SERVER_ERROR);
      }
      return response;
	}

	@RequestMapping(value = "/deleteAllSongsFromDb/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> deleteAllSongsFromDb(@PathVariable("songId") String songId,
			HttpServletRequest request) {
	  DbQueryStatus queryStatus  = playlistDriver.deleteSongFromDb(songId);
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		response = Utils.setResponseStatus(response, queryStatus.getdbQueryExecResult(),queryStatus.getData());
		return response;
	}
}