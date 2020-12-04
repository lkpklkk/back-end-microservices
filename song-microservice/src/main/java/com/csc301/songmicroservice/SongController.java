package com.csc301.songmicroservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/")
public class SongController {

  @Autowired
  private final SongDal songDal;

  private OkHttpClient client = new OkHttpClient();


  public SongController(SongDal songDal) {
    this.songDal = songDal;
  }


  @RequestMapping(value = "/getSongById/{songId}", method = RequestMethod.GET)
  public @ResponseBody Map<String, Object> getSongById(@PathVariable("songId") String songId,
      HttpServletRequest request) {

    Map<String, Object> response = new HashMap<String, Object>();
    response.put("path", String.format("GET %s", Utils.getUrl(request)));

    DbQueryStatus dbQueryStatus = songDal.findSongById(songId);

    response.put("message", dbQueryStatus.getMessage());
    response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(),
        dbQueryStatus.getData());

    return response;
  }


  @RequestMapping(value = "/getSongTitleById/{songId}", method = RequestMethod.GET)
  public @ResponseBody Map<String, Object> getSongTitleById(@PathVariable("songId") String songId,
      HttpServletRequest request) {
    Map<String, Object> response = new HashMap<String, Object>();
    response.put("path", String.format("GET %s", Utils.getUrl(request)));
    DbQueryStatus dbQueryStatus = songDal.getSongTitleById(songId);
    response.put("message", dbQueryStatus.getMessage());
    response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(),
        dbQueryStatus.getData());
    return response;
  }


  @RequestMapping(value = "/deleteSongById/{songId}", method = RequestMethod.DELETE)
  public @ResponseBody Map<String, Object> deleteSongById(@PathVariable("songId") String songId,
      HttpServletRequest request) {
    DbQueryStatus queryStatus = songDal.deleteSongById(songId);
    Map<String, Object> response = new HashMap<String, Object>();
    response.put("path", String.format("DELETE %s", Utils.getUrl(request)));
    response = Utils.setResponseStatus(response, queryStatus.getdbQueryExecResult(),
        queryStatus.getData());

    return response;
  }


  @RequestMapping(value = "/addSong", method = RequestMethod.POST)
  public @ResponseBody Map<String, Object> addSong(@RequestParam Map<String, String> params,
      HttpServletRequest request) {
    Map<String, Object> response = new HashMap<String, Object>();
    Song songToAdd;
    try {
      songToAdd = new Song(params.get("songName"), params.get("songArtistFullName"),
          params.get("songAlbum"));
    } catch (Exception e) {
      response.put("status", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
      return response;
    }
    DbQueryStatus queryStatus = songDal.addSong(songToAdd);
    response.put("message", queryStatus.getMessage());
    response = Utils.setResponseStatus(response, queryStatus.getdbQueryExecResult(),
        queryStatus.getData());
    return response;
  }


  @RequestMapping(value = "/updateSongFavouritesCount/{songId}", method = RequestMethod.PUT)
  public @ResponseBody Map<String, Object> updateFavouritesCount(
      @PathVariable("songId") String songId,
      @RequestParam("shouldDecrement") String shouldDecrement, HttpServletRequest request) {
    boolean sD;
    Map<String, Object> response = new HashMap<String, Object>();
    response.put("data", String.format("PUT %s", Utils.getUrl(request)));
    if (shouldDecrement.equalsIgnoreCase("true")) {
      sD = true;
    } else if (shouldDecrement.equalsIgnoreCase("false")) {
      sD = false;
    } else {
      response.put("status", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
      response.put("message", "invalid input");
      return response;
    }

    DbQueryStatus queryStatus = songDal.updateSongFavouritesCount(songId, sD);
    response.put("message", queryStatus.getMessage());
    response = Utils.setResponseStatus(response, queryStatus.getdbQueryExecResult(),
        queryStatus.getData());
    return response;
  }
}
