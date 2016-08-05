package com.app.waytogo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.app.waytogo.model.ProfileDetails;
import com.app.waytogo.support.UrlGenerator;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mikhaellopez.circularimageview.CircularImageView;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class ProfileActivity extends AppCompatActivity implements OnClickListener {

	private ConnectivityManager cm;
	private CircularImageView iv_profile;
	private EditText edt_fname, edt_lname, edt_email;
	private CheckedTextView cb_terms;
	private ImageButton btn_next;
	private String strFname, strLname, strEmail, strNumber, strCode, strResult, strFBFirstName, strFBLastName, strFBEmail, strLoginType;
	private Boolean isChecked;
	private static final int TAKENPHOTO = 0;
	private static final int SELECT_PHOTO = 1;
	private Bitmap bMap;
	private File photofile;
	private AlertDialog alertDialog;
	private SharedPreferences myPreferences, myGCMPreferences;
	private Editor editor,editorGCM;
	private String USER = "user_pref";
	private String GCM = "gcm_pref";
	private ProgressDialog pdialog;
	private String registrationId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_profile);

		try {
			Bundle b = getIntent().getExtras();
			strNumber = b.getString("strNumber");
			strCode = b.getString("strCode");

			strLoginType = b.getString("strLoginType");
			strFBFirstName = b.getString("strFirstName");
			strFBLastName = b.getString("strLastName");
			strFBEmail = b.getString("strEmail");

			
		} catch(NullPointerException e) {}
		
		getGCMDeviceId();
		
		cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		myPreferences = getSharedPreferences(USER, MODE_PRIVATE);
		editor = myPreferences.edit();
		myGCMPreferences = getSharedPreferences(GCM, Context.MODE_PRIVATE);
		editorGCM = myGCMPreferences.edit();
		
		iv_profile = (CircularImageView) findViewById(R.id.iv_profile);
		edt_fname = (EditText) findViewById(R.id.edt_fname);
		edt_lname = (EditText) findViewById(R.id.edt_lname);
		edt_email = (EditText) findViewById(R.id.edt_email);
		cb_terms = (CheckedTextView) findViewById(R.id.cb_terms);
		btn_next = (ImageButton) findViewById(R.id.btn_next);

		iv_profile.setOnClickListener(ProfileActivity.this);
		cb_terms.setOnClickListener(ProfileActivity.this);
		btn_next.setOnClickListener(ProfileActivity.this);
		
		try {
			if(strLoginType.equals("FB") == true) {
				edt_fname.setText(strFBFirstName);
				edt_lname.setText(strFBLastName);
				edt_email.setText(strFBEmail);
			}
		} catch(Exception e) {}
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
			case R.id.iv_profile:
				getImage();
				break;
		
			case R.id.tv_take_picture:
				alertDialog.dismiss();
				File folderPath = new File(Environment.getExternalStorageDirectory() + "/Rider");
				if (!folderPath.exists()) {
					folderPath.mkdirs();
				}
				photofile = new File(folderPath, (System.currentTimeMillis())+ ".jpg");
				Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // intent to start camera
				i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photofile));
				startActivityForResult(i, TAKENPHOTO);
				break;

			case R.id.tv_upload_picture:
				alertDialog.dismiss();
				Intent intent = new Intent(Intent.ACTION_PICK);
			    intent.setType("image/*");
			    startActivityForResult(Intent.createChooser(intent, "Complete action using"), SELECT_PHOTO);
				break;
				
			case R.id.cb_terms:
				if (cb_terms.isChecked()) {
					cb_terms.setChecked(false);
					isChecked = false;
				}
	    		else {
	    			cb_terms.setChecked(true);
	    			isChecked = true;
	    		}
				break;
				
			case R.id.btn_next:
				if (cm.getActiveNetworkInfo() != null
				&& cm.getActiveNetworkInfo().isAvailable()
				&& cm.getActiveNetworkInfo().isConnected()) {
					strFname = edt_fname.getText().toString();
					strLname = edt_lname.getText().toString();
					strEmail = edt_email.getText().toString();
					isChecked = cb_terms.isChecked();
					
					strEmail = strEmail.replace(" ", "");
		
					if (strFname.length() == 0 || strFname.toString().equals("")) {
						Toast.makeText(ProfileActivity.this, getResources().getString(R.string.strFnameEmpty), Toast.LENGTH_SHORT).show();
					} else if (strLname.length() == 0 	|| strLname.toString().equals("")) {
						Toast.makeText(ProfileActivity.this, getResources().getString(R.string.strLnameEmpty), Toast.LENGTH_SHORT).show();
					} else if (strEmail.length() == 0 || strEmail.toString().equals("")) {
						Toast.makeText(ProfileActivity.this, getResources().getString(R.string.strEmailEmpty), Toast.LENGTH_SHORT).show();
					} else if (emailValidator(strEmail) != true) {
						Toast.makeText(ProfileActivity.this, getResources().getString(R.string.strEmailInvalid), Toast.LENGTH_SHORT).show();
					} else if(!isChecked) { 
						Toast.makeText(ProfileActivity.this, getResources().getString(R.string.strAcceptTerms), Toast.LENGTH_SHORT).show();
					}
					else {
						String strGCM = myGCMPreferences.getString("strDeviceToken", "");
						new UserRegister(strFname, strLname, strEmail, strGCM).execute();
					}
				} else {
					checkInternetConnection();
				}
				break;
		}
	}
	
	private class UserRegister extends AsyncTask<String, Integer, String> {

		String str_fname, str_lname, str_email, str_gcm;
		
		public UserRegister(String strFname, String strLname, String strEmail, String strGCM) {
			// TODO Auto-generated constructor stub
			str_fname = strFname;
			str_lname = strLname;
			str_email = strEmail;
			str_gcm = strGCM;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			pdialog = new ProgressDialog(ProfileActivity.this);
			pdialog.setMessage("Loading...");
			pdialog.setCancelable(false);
			pdialog.show();
		}
		
		@SuppressWarnings("deprecation")
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				
				String url = null;
				try {
					url = UrlGenerator.userRegistration();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				
				byte[] data = null;
			    Date now = new Date(); // java.util.Date, NOT java.sql.Date or java.sql.Timestamp!
			    String format = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
			    String fileName = format+".jpg";
			    
			    HttpClient httpClient = new DefaultHttpClient();
			    HttpPost httpPost = new HttpPost(url);

			    MultipartEntity entity = new MultipartEntity();

			    try {
				    ByteArrayOutputStream bos = new ByteArrayOutputStream();
				    bMap.compress(CompressFormat.JPEG, 100, bos);
				    data = bos.toByteArray();
				} catch(Exception e) {
			    	bMap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_profile_pic);
			    	ByteArrayOutputStream bos = new ByteArrayOutputStream();
				    bMap.compress(CompressFormat.JPEG, 100, bos);
				    data = bos.toByteArray();
				}
			    
			    entity.addPart("vFirst", new StringBody(str_fname));
			    entity.addPart("vLast", new StringBody(str_lname));
			    entity.addPart("vPassword", new StringBody("phonelogin"));
			    entity.addPart("vEmail", new StringBody(str_email));
			    entity.addPart("userPhone", new StringBody(strNumber));
			    entity.addPart("vDeviceToken", new StringBody(str_gcm));
			    entity.addPart("vImage", new ByteArrayBody(data,"image/jpeg",fileName));

			    httpPost.setEntity(entity);
			    HttpResponse response = httpClient.execute(httpPost);
			    strResult = EntityUtils.toString(response.getEntity());
				
			} catch (ClientProtocolException e) {

			} catch (IOException e) {

			} /*catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			return strResult;
			
		}
		
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			pdialog.dismiss();
			if(result.equals("") == false)
			{
				try {
					
					JSONObject jObject;
					String strStatus = null, strMessage = null;
					try {
						jObject = new JSONObject(result);
						
						strStatus = jObject.optString("status");
						strMessage = jObject.optString("message");
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
						
					if(strStatus.equals("0") == true) {
						
						GsonBuilder gsonBuilder = new GsonBuilder();
						Gson gson = gsonBuilder.create();
		
						ProfileDetails profile = gson.fromJson(result, ProfileDetails.class);
						
						String strUserId = profile.data.iUserID;
						String strFirstName = profile.data.vFirst;
						String strLastName = profile.data.vLast;
						String strEmail = profile.data.vEmail;
						String strImageUrl = profile.data.profileImage.original;
						String strIsDriver = profile.data.vDriverorNot;
						String strUserPhone = profile.data.userPhone;
						String strDeviceToken = profile.data.vDeviceToken;
						String strToken = profile.data.vToken;
										
						editor.putString("strUserId", strUserId);
						editor.putString("strFirstName", strFirstName);
						editor.putString("strLastName", strLastName);
						editor.putString("strEmail", strEmail);
						editor.putString("strImageUrl", strImageUrl);
						editor.putString("strIsDriver", strIsDriver);
						editor.putString("strUserPhone", strUserPhone);
						editor.putString("strDeviceToken", strDeviceToken);
						editor.putString("strToken", strToken);
						editor.putString("isLogin", "1");
						editor.commit();
						
						Intent i_profile = new Intent(ProfileActivity.this, MainActivity.class);
						startActivity(i_profile);
						
						ProfileActivity.this.finish();
						
						Toast.makeText(ProfileActivity.this, strMessage, Toast.LENGTH_SHORT).show();
					} else {
						if(strMessage.equals("Phonenumber is already registered.") == true) {
							Intent i_phone = new Intent(ProfileActivity.this, PhoneNumberLoginActivity.class);
							startActivity(i_phone);
							ProfileActivity.this.finish();
						} else {
							Toast.makeText(ProfileActivity.this, strMessage, Toast.LENGTH_SHORT).show();
						}
					}
				} catch(NullPointerException e) { e.printStackTrace(); }
			}
		}
	
	}

	private void checkInternetConnection() {
		// TODO Auto-generated method stub
		new AlertDialog.Builder(ProfileActivity.this)
        .setTitle(R.string.title)
        .setMessage(R.string.message)
        .setCancelable(false)
        .setPositiveButton(R.string.strok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            	dialog.dismiss();
            }
        }).show();
	}
	
	private void getImage() {
		// TODO Auto-generated method stub
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ProfileActivity.this);
		LayoutInflater inflater = this.getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.custom_alert_image, null);
		dialogBuilder.setView(dialogView);

		TextView tv_take_picture = (TextView) dialogView.findViewById(R.id.tv_take_picture);
		TextView tv_upload_picture = (TextView) dialogView.findViewById(R.id.tv_upload_picture);
		
		tv_take_picture.setOnClickListener(ProfileActivity.this);
		tv_upload_picture.setOnClickListener(ProfileActivity.this);
		
		alertDialog = dialogBuilder.create();
		alertDialog.show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	
		if( (requestCode == TAKENPHOTO) && (resultCode == Activity.RESULT_OK) )
		{
			try {
				bMap = BitmapFactory.decodeFile(data.getExtras().get("data").toString());
				BitmapDrawable ob = new BitmapDrawable(getResources(), bMap);
				iv_profile.setBackgroundDrawable(ob);
			} catch (NullPointerException ex) {
				bMap = BitmapFactory.decodeFile(photofile.getAbsolutePath());
				BitmapDrawable ob = new BitmapDrawable(getResources(), bMap);
				iv_profile.setBackgroundDrawable(ob);
			}
		}
		
        if((requestCode == SELECT_PHOTO) && (resultCode == Activity.RESULT_OK))
        {  
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			bMap = BitmapFactory.decodeFile(picturePath);
			BitmapDrawable ob = new BitmapDrawable(getResources(), bMap);
			iv_profile.setBackgroundDrawable(ob);
        }
	}
	
	public static boolean emailValidator(final String mailAddress) {
		// TODO Auto-generated method stub
		Pattern pattern;
		Matcher matcher;

		final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

		pattern = Pattern.compile(EMAIL_PATTERN);
		matcher = pattern.matcher(mailAddress);
		return matcher.matches();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		try {
			if(strLoginType.equals("FB") == true) {
				Intent i_login = new Intent(ProfileActivity.this, LoginActivity.class);
				startActivity(i_login);
			} else {
				Toast.makeText(ProfileActivity.this, R.string.strRegisterSuccess, Toast.LENGTH_SHORT).show();
				Intent i_verification = new Intent(ProfileActivity.this, PhoneNumberVerificationActivity.class);
				i_verification.putExtra("strNumber", strNumber);
				i_verification.putExtra("strCode", strCode);
				i_verification.putExtra("strLoginType", strLoginType);
				i_verification.putExtra("strFirstName", strFBFirstName);
				i_verification.putExtra("strEmail", strFBEmail);
				startActivity(i_verification);
				ProfileActivity.this.finish();
			}
		} catch(Exception e) {}
	}
	
	
	public void getGCMDeviceId()
	{
		new AsyncTask<String, String, String>(){
			
			@Override
			protected String doInBackground(String... params) {
					
				String SENDER_ID = "651484448569";//"271579525703";
				GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(ProfileActivity.this);
				try {
					registrationId = gcm.register(SENDER_ID); 
					editorGCM.putString("strDeviceToken", registrationId);
					editorGCM.commit();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return registrationId;
			}
			
		}.execute();
		
	}
}
