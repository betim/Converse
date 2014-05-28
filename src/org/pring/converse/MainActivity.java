package org.pring.converse;

import org.pring.converse.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

@SuppressLint("ShowToast")
public class MainActivity extends Activity implements
    ActionBar.OnNavigationListener {
  private static DataBaseHelper dbHelper = null;
  public static View rootView = null;
  public static SQLiteDatabase database = null;

  private static int topicId = 0;
  private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item_id";

  private static String[] topics = new String[] { "All", "Normal", "Fun",
      "Philosophy", "Out there", "Love", "Naughty", "Personal" };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    dbHelper = new DataBaseHelper(getApplicationContext());

    setContentView(R.layout.activity_main);

    final ActionBar actionBar = getActionBar();
    actionBar.setDisplayShowTitleEnabled(false);
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
    actionBar.setDisplayShowHomeEnabled(false);
    actionBar.setDisplayShowTitleEnabled(false);

    actionBar.setListNavigationCallbacks(
        new ArrayAdapter<String>(actionBar.getThemedContext(),
            android.R.layout.simple_list_item_1, android.R.id.text1, topics),
        this);
  }

  @Override
  protected void onPause() {
    dbHelper.close();
    super.onPause();
  }

  @Override
  protected void onResume() {
    database = dbHelper.getWritableDatabase();
    super.onResume();
  }

  @Override
  public void onRestoreInstanceState(Bundle savedInstanceState) {
    if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
      getActionBar().setSelectedNavigationItem(
          savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
        .getSelectedNavigationIndex());
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    switch (id) {
    case R.id.get_new:
      new FetchTopic(getApplicationContext()).execute(topics[topicId] + "="
          + topicId);
      break;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onNavigationItemSelected(int position, long id) {
    topicId = position;

    getFragmentManager().beginTransaction()
        .replace(R.id.container, TopicFragment.newInstance()).commit();

    return true;
  }

  public static class TopicFragment extends Fragment {
    public static TopicFragment newInstance() {
      TopicFragment fragment = new TopicFragment();
      return fragment;
    }

    public TopicFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {

      rootView = inflater.inflate(R.layout.fragment_main, container, false);

      new FetchTopic(getActivity().getApplicationContext())
          .execute(topics[topicId] + "=" + topicId);
      return rootView;
    }
  }
}