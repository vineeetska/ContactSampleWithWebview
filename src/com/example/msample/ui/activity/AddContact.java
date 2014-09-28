package com.example.msample.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Intents;
import android.view.View;
import android.widget.EditText;

import com.example.msample.R;

public class AddContact extends Activity{

	private EditText mFirstName, mLastName, mPhone, mEmailAddress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_contact);
		mFirstName = (EditText) findViewById(R.id.firstName);
		mLastName = (EditText) findViewById(R.id.lastName);
		mPhone = (EditText) findViewById(R.id.phone);
		mEmailAddress = (EditText) findViewById(R.id.email);
	}
	
	public void onClickDone(View v){
		Intent intent = new Intent(Intents.Insert.ACTION);
		// Sets the MIME type to match the Contacts Provider
		intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
		intent.putExtra(Intents.Insert.NAME, mFirstName.getText().toString() + " " + mLastName.getText())
		.putExtra(Intents.Insert.EMAIL, mEmailAddress.getText())
		.putExtra(Intents.Insert.EMAIL_TYPE, CommonDataKinds.Email.TYPE_WORK)
		.putExtra(Intents.Insert.PHONE, mPhone.getText())
		.putExtra(Intents.Insert.PHONE_TYPE, Phone.TYPE_WORK);
		startActivity(intent);
		finish();
	}
	
	public void onClickCancel(View v){
		finish();
	}
	
}
