package com.example.msample.ui.activity;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.msample.R;
import com.example.msample.R.array;
import com.example.msample.R.id;
import com.example.msample.R.layout;
import com.example.msample.R.menu;
import com.example.msample.model.ContactModel;
import com.example.msample.util.ImageHelper;

public class MainActivity extends ListActivity implements LoaderCallbacks<Cursor>{

	@SuppressLint("InlinedApi")
	private final static String[] PROJECTION = {
		Contacts._ID,
		Contacts.LOOKUP_KEY,
		Build.VERSION.SDK_INT
		>= Build.VERSION_CODES.HONEYCOMB ?
				Contacts.DISPLAY_NAME_PRIMARY :
					Contacts.DISPLAY_NAME,
	    Contacts.HAS_PHONE_NUMBER
	};


	public static ArrayList<ContactModel> sContactList = null;
	private ListView mSortListView;
	private TextView memptyView;
	private ContactListAdapter mAdapter;
	protected ProgressDialog mProgressDialog;
	private Button mAddMoreContact;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		memptyView = (TextView)findViewById(R.id.empty_txt);
		mSortListView = (ListView)findViewById(R.id.sort_list);
		mAddMoreContact = (Button)findViewById(R.id.addContact);

		ArrayAdapter<String> sortAadapter = new ArrayAdapter<String>(MainActivity.this, R.layout.alpha_list_item, getResources().getStringArray(R.array.alphabets));
		mSortListView.setAdapter(sortAadapter);

		mSortListView.setOnItemClickListener(new ListView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				View v = getListView().getChildAt(0);
				int top = (v == null) ? 0 : v.getTop();
				int position = getCharIndex(mSortListView.getItemAtPosition(arg2).toString());
				if(position >= 0){
					getListView().setSelectionFromTop(position, top);
					getListView().setSelection(position);
				}
			}
		});

		getListView().setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(getApplicationContext(), ContactDetailWebview.class);
				intent.putExtra("pos", arg2);
				startActivity(intent);

			}
		});

		mAddMoreContact.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				startActivity(new Intent(getApplicationContext(), AddContact.class));
			}
		});

		mProgressDialog = ProgressDialog.show(MainActivity.this, "", "Please wait...", true, false);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL | ProgressDialog.STYLE_SPINNER);

		getLoaderManager().initLoader(1, null, this);

	}
	
	private int getCharIndex(String txt){
		for (int i = 0; i < sContactList.size(); i++)
			if (sContactList.get(i).getName().equalsIgnoreCase(txt))
				return i;
		
		return sContactList.size() - 1;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// TODO Auto-generated method stub
		String selection = Contacts.HAS_PHONE_NUMBER + "=1";
		return new CursorLoader(getApplicationContext(), Contacts.CONTENT_URI, PROJECTION, selection, null, Build.VERSION.SDK_INT
				>= Build.VERSION_CODES.HONEYCOMB ?
						Contacts.DISPLAY_NAME_PRIMARY :
							Contacts.DISPLAY_NAME + " ASC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, final Cursor cursor) {
		// TODO Auto-generated method stub
		if (cursor != null && cursor.getCount() > 0){
			new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					cursor.moveToFirst();
					sContactList = new ArrayList<ContactModel>();
					do {
						ContactModel contact = new ContactModel();
						contact.setId(cursor.getLong(cursor.getColumnIndex(Contacts._ID)));
						contact.setName(cursor.getString(cursor.getColumnIndex(Build.VERSION.SDK_INT
								>= Build.VERSION_CODES.HONEYCOMB ?
										Contacts.DISPLAY_NAME_PRIMARY :
											Contacts.DISPLAY_NAME)));

						Cursor phone = getContentResolver().query(CommonDataKinds.Phone.CONTENT_URI, null, 
								CommonDataKinds.Phone.CONTACT_ID +" = "+ contact.getId(), null, null);
						if (phone != null && phone.getCount() > 0){
							phone.moveToFirst();
							contact.setPhone(phone.getString(phone.getColumnIndex(CommonDataKinds.Phone.NUMBER)));
						}

						if (phone != null && !phone.isClosed())
							phone.close();

						Cursor email = getContentResolver().query(CommonDataKinds.Email.CONTENT_URI, null, 
								CommonDataKinds.Phone.CONTACT_ID +" = "+ contact.getId(), null, null);
						if (email != null && email.getCount() > 0){
							email.moveToFirst();
							contact.setEmail(email.getString(email.getColumnIndex(CommonDataKinds.Email.ADDRESS)));
						}

						if (email != null && !email.isClosed())
							email.close();

						Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contact.getId());
						Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
						Cursor photo = getContentResolver().query(photoUri,
								new String[] {Contacts.Photo.PHOTO}, null, null, null);
						if (photo != null && photo.getCount() > 0 && photo.moveToFirst()) {
							contact.setPic(photo.getBlob(0));
						}

						if (photo != null && !photo.isClosed())
							photo.close();

						sContactList.add(contact);
					}while(cursor.moveToNext());

					sortContacts();

					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							setAdapter();
						}
					});
				}
			}).start();

		}

	}

	private void sortContacts(){
		int size = sContactList.size();
		ArrayList<ContactModel> list = new ArrayList<ContactModel>();
		for(int i = 0; i < 26; i++){
			for(int k = 0; k < size; k++){
				if(sContactList.get(k).getName().startsWith(String.valueOf(Character.toChars(65 + i)))){
					if(contains(list, String.valueOf(Character.toChars(65 + i)))){
						list.add(sContactList.get(k));
					}else{
						ContactModel c = new ContactModel();
						c.setName(String.valueOf(Character.toChars(65 + i)));
						list.add(c);
						list.add(sContactList.get(k));
					}
				}
			}
		}
		
		sContactList = null;
		sContactList = list;
	}

	private boolean contains(ArrayList<ContactModel> list, String name){
		for (ContactModel model : list)
			if (model.getName().equals(name))
				return true;
		return false;
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub

	}

	private void setAdapter(){
		if (mProgressDialog != null)
			mProgressDialog.dismiss();

		mAdapter = new ContactListAdapter();
		getListView().setAdapter(mAdapter);
		getListView().setEmptyView(memptyView);
	}

	class ContactListAdapter extends BaseAdapter{

		private LayoutInflater inflator;
		private ArrayList<String> mDisabledValues;
		private ArrayList<Integer> mDisabledPosition;

		public ContactListAdapter() {
			// TODO Auto-generated constructor stub
			inflator = LayoutInflater.from(getApplicationContext());
			mDisabledValues = new ArrayList<String>();
			mDisabledPosition = new ArrayList<Integer>();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (sContactList == null)
				return 0;
			return sContactList.size();
		}

		@Override
		public ContactModel getItem(int arg0) {
			// TODO Auto-generated method stub
			return sContactList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public boolean isEnabled(int position) {
			if(!checkChar(getItem(position).toString(), position)){
				return false;
			}
			return super.isEnabled(position);
		}
		@Override
		public View getView(int position, View convertView, ViewGroup arg2) {
			// TODO Auto-generated method stub
			ViewHolder holder;
			if(convertView == null){
				holder = new ViewHolder();
				convertView = inflator.inflate(R.layout.list_item_layout, null);
				holder.background = (LinearLayout)convertView.findViewById(R.id.background);
				holder.pic = (ImageView)convertView.findViewById(R.id.pic);
				holder.name = (TextView)convertView.findViewById(R.id.name);
				convertView.setTag(holder);
			}
			else{
				holder = (ViewHolder)convertView.getTag();
			}

			if(!isEnabled(position)){
				holder.pic.setVisibility(View.GONE);
				holder.background.setBackgroundColor(getResources().getColor(android.R.color.background_dark));
				holder.name.setTextColor(getResources().getColor(android.R.color.white));
			}else{
				holder.pic.setVisibility(View.VISIBLE);
				holder.background.setBackgroundColor(getResources().getColor(android.R.color.white));
				holder.name.setTextColor(getResources().getColor(android.R.color.black));
			}

			if (getItem(position) != null && getItem(position).getPic() != null){
				holder.pic.setImageBitmap(ImageHelper.decodeSampledBitmapFromByte(getItem(position).getPic(), 48, 48));
			}

			holder.name.setText(getItem(position).getName());

			return convertView;
		}

		class ViewHolder{
			LinearLayout background;
			ImageView pic;
			TextView name;
		}

		private boolean checkChar(String txt, int position){
			boolean ret_val = true;
			for(int i = 0; i < 26; i++){
				try{
					if(String.valueOf(Character.toChars(65 + i)).equals(txt)){
						if(mDisabledValues.contains(txt)){
							if(mDisabledPosition.contains(position)){
								ret_val = false;
							}else{
								ret_val = true;
							}

							break;
						}else{
							mDisabledValues.add(txt);
							mDisabledPosition.add(position);
							ret_val = false;
							break;
						}
					}
				}catch(Exception e){ }
			}

			return ret_val;
		}

	}

}
