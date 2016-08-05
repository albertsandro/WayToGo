package com.app.waytogo.fragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.waytogo.R;
import com.app.waytogo.model.ProfileDetails;
import com.app.waytogo.support.UrlGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.picasso.Picasso;

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
import java.util.Calendar;
import java.util.Date;

@SuppressWarnings("deprecation")
public class ProfileFragment extends Fragment {

	private ConnectivityManager cm;
	private EditText edt_full_name;
	private EditText edt_security_number;
	private static EditText edt_dob;
	private EditText edt_license_number;
	private ImageButton btn_next, iv_dob, btn_yes, btn_no;
	private ImageView iv_driver_profile;
	private RelativeLayout rl_profile_pic;
	private SharedPreferences myPreferences, myUserPreferences;
	private Editor editor;
	private String DRIVER = "driver_pref";
	private String USER = "user_pref";
	private String strFullName, strSecurity, strDOB, strLicenseNumber, strPicturePath;
	private DatePickerFragment dpResult;
	private int year, month, day;
	static final int DATE_DIALOG_ID = 999;
	private static final int TAKENPHOTO = 0;
	private static final int SELECT_PHOTO = 1;
	private Bitmap bMap, bPhoto;
	private File photofile;
	private AlertDialog alertDialog;
	private ProgressDialog pdialog;
	private String strResult;
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_page_driver_personal_info, container, false);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		
		myPreferences = getActivity().getSharedPreferences(DRIVER, Context.MODE_PRIVATE);
		myUserPreferences = getActivity().getSharedPreferences(USER, Context.MODE_PRIVATE);
		editor = myPreferences.edit();
		
		rl_profile_pic = (RelativeLayout) getView().findViewById(R.id.rl_profile_pic);
		edt_full_name = (EditText) getView().findViewById(R.id.edt_full_name);
		edt_security_number = (EditText) getView().findViewById(R.id.edt_security_number);
		edt_dob = (EditText) getView().findViewById(R.id.edt_dob);
		edt_license_number = (EditText) getView().findViewById(R.id.edt_license_number);
		btn_next = (ImageButton) getView().findViewById(R.id.btn_next);
		iv_dob = (ImageButton) getView().findViewById(R.id.iv_dob);
		btn_yes = (ImageButton) getView().findViewById(R.id.btn_yes);
		btn_no = (ImageButton) getView().findViewById(R.id.btn_no);
		iv_driver_profile = (ImageView) getView().findViewById(R.id.iv_driver_profile);
		
		String strDriverImageUrl = myPreferences.getString("strDriverImage", "");
		if(strDriverImageUrl.equals("") == false)
		{
			if(strDriverImageUrl.startsWith("http"))
				Picasso.with(getActivity()).load(strDriverImageUrl).centerCrop().resize(75, 75).into(iv_driver_profile);
			else
				Picasso.with(getActivity()).load(getResources().getString(R.string.strDriverBaseImageUrl)+""+strDriverImageUrl).centerCrop().resize(75, 75).into(iv_driver_profile);
		}
		
		iv_dob.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DialogFragment picker = new DatePickerFragment();
				picker.show(getFragmentManager(), "datePicker");
			}
		});
		
		btn_next.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				strFullName = edt_full_name.getText().toString();
				strSecurity = edt_security_number.getText().toString();
				strDOB = edt_dob.getText().toString();
				strLicenseNumber = edt_license_number.getText().toString();
				
				if (strFullName.length() == 0 || strFullName.toString().equals("")) {
					Toast.makeText(getActivity(), getResources().getString(R.string.strFullNameEmpty), Toast.LENGTH_SHORT).show();
				} else if (strDOB.length() == 0 || strDOB.toString().equals("")) {
					Toast.makeText(getActivity(), getResources().getString(R.string.strDOBEmpty), Toast.LENGTH_SHORT).show();
				}  else if (strLicenseNumber.length() == 0 || strLicenseNumber.toString().equals("")) {
					Toast.makeText(getActivity(), getResources().getString(R.string.strLicenseNumberEmpty), Toast.LENGTH_SHORT).show();
				}  else {
					editor.putString("strFullName", strFullName);
					editor.putString("strSecurity", strSecurity);
					editor.putString("strDOB", strDOB);
					editor.putString("strLicenseNumber", strLicenseNumber);
					editor.commit();
					
					rl_profile_pic.setVisibility(View.VISIBLE);
				}
			}
		});
		
		btn_yes.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
					new UploadProfilePics().execute();
				} else {
					networkConnectivity();
				}
			}
		});

		btn_no.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getImage();
			}
		});
	}

	private void getImage() {
		// TODO Auto-generated method stub
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.custom_alert_image, null);
		dialogBuilder.setView(dialogView);

		TextView tv_take_picture = (TextView) dialogView.findViewById(R.id.tv_take_picture);
		TextView tv_upload_picture = (TextView) dialogView.findViewById(R.id.tv_upload_picture);
		
		tv_take_picture.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alertDialog.dismiss();
				File folderPath = new File(Environment.getExternalStorageDirectory() + "/Rider");
				if (!folderPath.exists()) {
					folderPath.mkdirs();
				}
				photofile = new File(folderPath, (System.currentTimeMillis())+ ".jpg");
				Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // intent to start camera
				i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photofile));
				startActivityForResult(i, TAKENPHOTO);
			}
		});
		
		tv_upload_picture.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alertDialog.dismiss();
				Intent intent = new Intent(Intent.ACTION_PICK);
			    intent.setType("image/*");
			    startActivityForResult(Intent.createChooser(intent, "Complete action using"), SELECT_PHOTO);
			}
		});
		
		alertDialog = dialogBuilder.create();
		alertDialog.show();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	
		if( (requestCode == TAKENPHOTO) && (resultCode == Activity.RESULT_OK) )
		{
			try {
				strPicturePath = data.getExtras().get("data").toString();
				bMap = BitmapFactory.decodeFile(data.getExtras().get("data").toString());
				BitmapDrawable ob = new BitmapDrawable(getResources(), bMap);
				iv_driver_profile.setBackgroundDrawable(ob);
				editor.putString("strDriverPicturePath", strPicturePath);
				editor.commit();
				
			} catch (NullPointerException ex) {
				strPicturePath = photofile.getAbsolutePath().toString();
				bMap = BitmapFactory.decodeFile(photofile.getAbsolutePath());
				BitmapDrawable ob = new BitmapDrawable(getResources(), bMap);
				iv_driver_profile.setBackgroundDrawable(ob);
				editor.putString("strDriverPicturePath", strPicturePath);
				editor.commit();
			}

		}
		
        if((requestCode == SELECT_PHOTO) && (resultCode == Activity.RESULT_OK))
        {  
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Cursor cursor = getActivity().getContentResolver().query(selectedImage,filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			strPicturePath = cursor.getString(columnIndex);
			bMap = BitmapFactory.decodeFile(strPicturePath);
			BitmapDrawable ob = new BitmapDrawable(getResources(), bMap);
			iv_driver_profile.setBackgroundDrawable(ob);
			editor.putString("strDriverPicturePath", strPicturePath);
			editor.commit();
        }
	}
	
	public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener
	{
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = Calendar.getInstance();
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);
		
			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}
		
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			// TODO Auto-generated method stub
			Calendar c = Calendar.getInstance();
			c.set(year, monthOfYear, dayOfMonth);
		
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String formattedDate = sdf.format(c.getTime());
			edt_dob.setText(formattedDate.toString());
		}
	}
	
	private class UploadProfilePics extends AsyncTask<String, Integer, String> {

		String str_fname, str_lname, str_userid;
		
		public UploadProfilePics() {
			// TODO Auto-generated constructor stub
			str_userid = myUserPreferences.getString("strUserId", "");
			str_fname = myUserPreferences.getString("strFirstName", "");
			str_lname = myUserPreferences.getString("strLastName", "");
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			pdialog = new ProgressDialog(getActivity());
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
					url = UrlGenerator.driverProfileRegistration();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				
				byte[] data = null;
			    Date now = new Date(); // java.util.Date, NOT java.sql.Date or java.sql.Timestamp!
			    String format = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
			    String fileName =format+".jpg";
			    
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
			    entity.addPart("iUserID", new StringBody(str_userid));
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
					
					try 
					{
						JSONObject jObject = new JSONObject(result);
							
						String strStatus = jObject.optString("status");
						String strMessage = jObject.optString("message");
						
						if(strStatus.equals("0") == true) 
						{	
							GsonBuilder gsonBuilder = new GsonBuilder();
							Gson gson = gsonBuilder.create();
			
							ProfileDetails profile = gson.fromJson(result, ProfileDetails.class);
							
							String strUserId = profile.data.iUserID;
							String strFirstName = profile.data.vFirst;
							String strLastName = profile.data.vLast;
							String strEmail = profile.data.vEmail;
							String strDriverImage = profile.data.vImage;
							String strImageUrl = profile.data.profileImage.original;
							String strIsDriver = profile.data.vDriverorNot;
							String strUserPhone = profile.data.userPhone;
							
							editor.putString("strUserId", strUserId);
							editor.putString("strFirstName", strFirstName);
							editor.putString("strLastName", strLastName);
							editor.putString("strEmail", strEmail);
							editor.putString("strDriverImage", strDriverImage);
							editor.putString("strImageUrl", strImageUrl);
							editor.putString("strIsDriver", strIsDriver);
							editor.putString("strUserPhone", strUserPhone);
							editor.putString("isLogin", "1");
							editor.commit();
							
							Fragment fragment = new VehicleFragment();
							
							Bundle bundle = new Bundle();
							bundle.putString("strUserId", strUserId);
							bundle.putString("strDriverImage", strDriverImage);
							fragment.setArguments(bundle);
							
							FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
							FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
							fragmentTransaction.add(R.id.container_body, fragment);
							fragmentTransaction.addToBackStack("FragmentC");
							fragmentTransaction.commit();
							
						} else {
							Toast.makeText(getActivity(), strMessage, Toast.LENGTH_SHORT).show();
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch(NullPointerException e) { e.printStackTrace(); }
			}
		}
	
	}

	private void networkConnectivity()
	{
        new AlertDialog.Builder(getActivity())
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
}
