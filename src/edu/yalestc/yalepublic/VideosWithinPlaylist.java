package edu.yalestc.yalepublic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.yalestc.yalepublic.VideoList.PlaceholderFragment;
import edu.yalestc.yalepublic.VideoList.VideoTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Activity;
import android.app.Fragment;
import android.content.ClipData.Item;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

public class VideosWithinPlaylist extends Activity {
    private String[] titls;
    private String[] dats;
    private Bitmap[] bitmaps;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();
        setContentView(R.layout.activity_video_list);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }
}
    
    public class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_video_within_list,
                    container, false);
            
            // initialize the custom adapter
            thumbnailAdapter adapter = new thumbnailAdapter(getActivity());
            //        getActivity(), R.layout.tab, R.id.tab);
            
            // create an asynctask that fetches the playlist titles and much more
          /*  VideoTask videoList = new VideoTask();
            videoList.execute();
            
            ListView listView = (ListView) rootView.findViewById(R.id.listview_video);
            listView.setAdapter(mVideoAdapter);
            listView.setOnItemClickListener(new OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1,
                        int arg2, long arg3) {
                    Intent showThem = new Intent(VideoList.this, VideosWithinPlaylist.class);
                    showThem.putExtra("playlistId", playlistIds[arg2]);
                    Log.v("StartingActivityInVideoList",playlistIds[arg2]);
                    startActivity(showThem);
                }
            });*/
            
            
            return rootView;
        }
    }
    
    public class thumbnailAdapter extends BaseAdapter{
        Context mContext;
            thumbnailAdapter(Context context) {
                mContext = context;
            }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            
            if(convertView != null){
                ((ImageView)((RelativeLayout) convertView).getChildAt(1)).setImageBitmap(bitmaps[position]);
                ((TextView)((RelativeLayout) convertView).getChildAt(2)).setText(titls[position]);
                ((TextView)((RelativeLayout) convertView).getChildAt(3)).setText(dats[position]);
                return convertView;
            } else {
                RelativeLayout thumbnail = (RelativeLayout) getLayoutInflater().inflate(R.id.thumbnailView, parent);
                ((ImageView)thumbnail.getChildAt(1)).setImageBitmap(bitmaps[position]);
                ((TextView)thumbnail.getChildAt(2)).setText(titls[position]);
                ((TextView)thumbnail.getChildAt(3)).setText(dats[position]);
                return thumbnail;
            } 
            
        }

        @Override
        public int getCount() {
            return titls.length;
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }
    }
    
    public class VideoTask extends AsyncTask<Void, Void, Void> {

        // this method parses the raw data (which is a String in JSON format)
        // and extracts the titles of the playlists
        private String getVideosFromJson(String rawData){
            JSONObject videoData;
            try {
                videoData = new JSONObject(rawData);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
            JSONArray playlistData;
            try {
                playlistData = videoData.getJSONArray("items");
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
            
            titls = new String[playlistData.length()];
            bitmaps = new Bitmap[playlistData.length()];
            dats = new String[playlistData.length()];
            
            String[] allVideosData = new String[playlistData.length()];
            for (int i = 0; i < playlistData.length(); i++){
                try {
                    titls[i] = playlistData.getJSONObject(i)
                            .getJSONObject("snippet")
                            .getString("title");
                    dats[i] = (playlistData.getJSONObject(i)
                            .getJSONObject("snippet")
                            .getString("publishedAt")).substring(0,9);
                
                    try {
                        URL imageUrl = new URL(playlistData.getJSONObject(i)
                                .getJSONObject("snippet")
                                .getJSONObject("thumbnails")
                                .getJSONObject("medium")
                                .getString("url"));
                 
                        HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
                        conn.setConnectTimeout(30000);
                        conn.setReadTimeout(30000);
                        conn.setInstanceFollowRedirects(true);
                        InputStream is=conn.getInputStream();
                        bitmaps[i] = BitmapFactory.decodeStream(is);
                    
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return null;
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                return null;
          }
        
            }
            return "1";
        }
        
        @Override
        protected Void doInBackground(Void... params) {
            try{
                // first we create the URI
                final String BASE_URL = "https://www.googleapis.com/youtube/v3/playlistsItems?";
                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter("part", "snippet")
                        .appendQueryParameter("playlistId", intent.getStringExtra("playlistId"))
                        .appendQueryParameter("key", new DeveloperKey().DEVELOPER_KEY)
                        .appendQueryParameter("maxResults", "50")
                        .build();
                
                // send a GET request to the server
                URL url = new URL(builtUri.toString());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // read all the data
                InputStream inputStream = urlConnection.getInputStream();                
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                String videosJsonStr = buffer.toString();
                // we pass the data to getPlaylistsFromJson
                //but also remember to save the playlistID's for future
                getVideosFromJson(videosJsonStr);
                
                // TODO check if there are more than 50 videos in the arrays
            }
            
            catch (IOException e){
                Log.e("URI", "uri was invalid or api request failed");
                e.printStackTrace();
                return null;
            }
            return null;
        }
        
        @Override
        protected void onPostExecute(Void result){

        }
    }

}
