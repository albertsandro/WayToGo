package com.app.waytogo.fragment;

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
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.waytogo.MainActivity;
import com.app.waytogo.R;
import com.app.waytogo.model.VehicleDetails;
import com.app.waytogo.support.UrlGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;


@SuppressWarnings("deprecation")
public class VehicleFragment extends Fragment {
	
	private ConnectivityManager cm;
	private TextView tv_vehicle_registration, tv_vehicle_insurance, tv_vehicle_pic_title, tv_vehicle_pic; 
	private EditText edt_licence_plate_number, edt_vehicle_type;
	private ImageButton btn_complete, btn_picture;
	private RelativeLayout rl_vehicle_pic;
	private SharedPreferences myPreferences;
	private Editor editor;
	private String USER = "user_pref";
	private AlertDialog alertDialog;
	private static final int REGISTRATION_TAKEN_PHOTO = 0;
	private static final int INSURANCE_TAKEN_PHOTO = 1;
	private static final int CAR_TAKEN_PHOTO = 2;
	private static final int REGISTRATION_SELECT_PHOTO = 3;
	private static final int INSURANCE_SELECT_PHOTO = 4;
	private static final int CAR_SELECT_PHOTO = 5;
	private File photofileRegistration, photoFileInsurance, photoFileCar;
	private String strPicturePathRegistration, strPicturePathInsurance, strPicturePathCar, strLicensePlate, strVehicleType, strResult, strDriverImage, strUserId;
	private ProgressDialog pdDialog;	
	private Bitmap bRegistrationPhoto, bInsurancePhoto, bCarPhoto;
		
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		try {
			strUserId = getArguments().getString("strUserId");
			strDriverImage = getArguments().getString("strDriverImage");
		} catch(Exception e) {}
		return inflater.inflate(R.layout.fragment_page_driver_vehicle_info, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);

		myPreferences = getActivity().getSharedPreferences(USER, Context.MODE_PRIVATE);
		editor = myPreferences.edit();
		
		cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		
		tv_vehicle_registration = (TextView) getView().findViewById(R.id.tv_vehicle_registration);
		tv_vehicle_insurance = (TextView) getView().findViewById(R.id.tv_vehicle_insurance);
		tv_vehicle_pic_title = (TextView) getView().findViewById(R.id.tv_vehicle_pic_title);
		tv_vehicle_pic = (TextView) getView().findViewById(R.id.tv_vehicle_pic);
		edt_licence_plate_number = (EditText) getView().findViewById(R.id.edt_licence_plate_number);
		edt_vehicle_type = (EditText) getView().findViewById(R.id.edt_vehicle_type);
		btn_complete = (ImageButton) getView().findViewById(R.id.btn_complete);
		btn_picture = (ImageButton) getView().findViewById(R.id.btn_picture);
		rl_vehicle_pic = (RelativeLayout) getView().findViewById(R.id.rl_vehicle_pic);
		
		tv_vehicle_registration.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getRegistrationImage();
			}
		});
		
		tv_vehicle_insurance.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getInsuranceImage();
			}
		});
		
		btn_complete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
					
				strLicensePlate = edt_licence_plate_number.getText().toString();
				strVehicleType = edt_vehicle_type.getText().toString();
				
				if (strPicturePathRegistration.length() == 0 || strPicturePathRegistration.toString().equals("")) {
					Toast.makeText(getActivity(), getResources().getString(R.string.strRegistrationEmpty), Toast.LENGTH_SHORT).show();
				} else if (strPicturePathInsurance.length() == 0 || strPicturePathInsurance.toString().equals("")) {
					Toast.makeText(getActivity(), getResources().getString(R.string.strLicenseEmpty), Toast.LENGTH_SHORT).show();
				}  else if (strLicensePlate.length() == 0 || strLicensePlate.toString().equals("")) {
					Toast.makeText(getActivity(), getResources().getString(R.string.strLicenseNumberEmpty), Toast.LENGTH_SHORT).show();
				} else if (strVehicleType.length() == 0 || strVehicleType.toString().equals("")) {
					Toast.makeText(getActivity(), getResources().getString(R.string.strVehicleTypeEmpty), Toast.LENGTH_SHORT).show();
				} else {
					editor.putString("strPicturePathRegistration", strPicturePathRegistration);
					editor.putString("strPicturePathInsurance", strPicturePathInsurance);
					editor.putString("strLicensePlate", strLicensePlate);
					editor.putString("strVehicleType", strVehicleType);
					editor.commit();
					
					rl_vehicle_pic.setVisibility(View.VISIBLE);
				}
			}
		});
		
		btn_picture.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				getCarImage();
			}
		});
	}
	
	public void showDriverInactiveAlert()
	{
		new AlertDialog.Builder(getActivity())
		.setMessage(R.string.strdriveralert)
		.setCancelable(false)
		.setPositiveButton(R.string.strgotit,
				new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					editor.putString("strIsDriver", "pending");
					editor.commit();
					Intent i_verification = new Intent(getActivity(), MainActivity.class);
					startActivity(i_verification);
					getActivity().finish();
				}
			})
		.show();
	}
	
	private void getRegistrationImage() {
		// TODO Auto-generated method stub
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.custom_alert_image, null);
		dialogBuilder.setView(dialogView);

		TextView tv_take_picture = (TextView) dialogView.findViewById(R.id.tv_take_picture);
		TextView tv_upload_picture = (TextView) dialogView.findViewById(R.id.tv_upload_picture);
		
		tv_take_picture.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alertDialog.dismiss();
				File folderPath = new File(Environment.getExternalStorageDirectory() + "/Rider");
				if (!folderPath.exists()) {
					folderPath.mkdirs();
				}
				photofileRegistration = new File(folderPath, (System.currentTimeMillis())+ ".jpg");
				Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // intent to start camera
				i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photofileRegistration));
				startActivityForResult(i, REGISTRATION_TAKEN_PHOTO);
			}
		});
		
		tv_upload_picture.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alertDialog.dismiss();
				Intent intent = new Intent(Intent.ACTION_PICK);
			    intent.setType("image/*");
			    startActivityForResult(Intent.createChooser(intent, "Complete action using"), REGISTRATION_SELECT_PHOTO);
			}
		});
		
		alertDialog = dialogBuilder.create();
		alertDialog.show();
	}
	
	private void getInsuranceImage() {
		// TODO Auto-generated method stub
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.custom_alert_image, null);
		dialogBuilder.setView(dialogView);

		TextView tv_take_picture = (TextView) dialogView.findViewById(R.id.tv_take_picture);
		TextView tv_upload_picture = (TextView) dialogView.findViewById(R.id.tv_upload_picture);
		
		tv_take_picture.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alertDialog.dismiss();
				File folderPath = new File(Environment.getExternalStorageDirectory() + "/Rider");
				if (!folderPath.exists()) {
					folderPath.mkdirs();
				}
				photoFileInsurance = new File(folderPath, (System.currentTimeMillis())+ ".jpg");
				Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // intent to start camera
				i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFileInsurance));
				startActivityForResult(i, INSURANCE_TAKEN_PHOTO);
			}
		});
		
		tv_upload_picture.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alertDialog.dismiss();
				Intent intent = new Intent(Intent.ACTION_PICK);
			    intent.setType("image/*");
			    startActivityForResult(Intent.createChooser(intent, "Complete action using"), INSURANCE_SELECT_PHOTO);
			}
		});
		
		alertDialog = dialogBuilder.create();
		alertDialog.show();
	}
	
	private void getCarImage() {
		// TODO Auto-generated method stub
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.custom_alert_image, null);
		dialogBuilder.setView(dialogView);

		TextView tv_take_picture = (TextView) dialogView.findViewById(R.id.tv_take_picture);
		TextView tv_upload_picture = (TextView) dialogView.findViewById(R.id.tv_upload_picture);
		
		tv_take_picture.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alertDialog.dismiss();
				File folderPath = new File(Environment.getExternalStorageDirectory() + "/Rider");
				if (!folderPath.exists()) {
					folderPath.mkdirs();
				}
				photoFileCar = new File(folderPath, (System.currentTimeMillis())+ ".jpg");
				Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // intent to start camera
				i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFileCar));
				startActivityForResult(i, CAR_TAKEN_PHOTO);
			}
		});
		
		tv_upload_picture.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				alertDialog.dismiss();
				Intent intent = new Intent(Intent.ACTION_PICK);
			    intent.setType("image/*");
			    startActivityForResult(Intent.createChooser(intent, "Complete action using"), CAR_SELECT_PHOTO);
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
	
		if((requestCode == REGISTRATION_TAKEN_PHOTO) && (resultCode == Activity.RESULT_OK))
		{
			try {
				strPicturePathRegistration = data.getExtras().get("data").toString();
				bRegistrationPhoto = BitmapFactory.decodeFile(data.getExtras().get("data").toString());
				tv_vehicle_registration.setBackgroundColor(Color.parseColor("#15BB18"));
				tv_vehicle_registration.setText(getResources().getString(R.string.strUploaded));
				editor.putString("strRegistrationPicturePath", strPicturePathRegistration);
				editor.commit();
				
			} catch (NullPointerException ex) {
				strPicturePathRegistration = photofileRegistration.getAbsolutePath();
				bRegistrationPhoto = BitmapFactory.decodeFile(photofileRegistration.getAbsolutePath());
				tv_vehicle_registration.setBackgroundColor(Color.parseColor("#15BB18"));
				tv_vehicle_registration.setText(getResources().getString(R.string.strUploaded));
				editor.putString("strRegistrationPicturePath", strPicturePathRegistration);
				editor.commit();
			}

		}
		
		if((requestCode == INSURANCE_TAKEN_PHOTO) && (resultCode == Activity.RESULT_OK))
		{
			try {
				strPicturePathInsurance = data.getExtras().get("data").toString();
				bInsurancePhoto = BitmapFactory.decodeFile(data.getExtras().get("data").toString());
				tv_vehicle_insurance.setBackgroundColor(Color.parseColor("#15BB18"));
				tv_vehicle_insurance.setText(getResources().getString(R.string.strUploaded));
				editor.putString("strInsurancePicturePath", strPicturePathInsurance);
				editor.commit();
				
			} catch (NullPointerException ex) {
				strPicturePathInsurance = photoFileInsurance.getAbsolutePath();
				bInsurancePhoto = BitmapFactory.decodeFile(photoFileInsurance.getAbsolutePath());
				tv_vehicle_insurance.setBackgroundColor(Color.parseColor("#15BB18"));
				tv_vehicle_insurance.setText(getResources().getString(R.string.strUploaded));
				editor.putString("strInsurancePicturePath", strPicturePathInsurance);
				editor.commit();
			}

		}
		
		if((requestCode == CAR_TAKEN_PHOTO) && (resultCode == Activity.RESULT_OK))
		{
			try {
				strPicturePathCar = data.getExtras().get("data").toString();
				bCarPhoto = BitmapFactory.decodeFile(data.getExtras().get("data").toString());
				tv_vehicle_insurance.setBackgroundColor(Color.parseColor("#15BB18"));
				tv_vehicle_insurance.setText(getResources().getString(R.string.strUploaded));
				editor.putString("strCarPicturePath", strPicturePathCar);
				editor.commit();
				
			} catch (NullPointerException ex) {
				strPicturePathCar = photoFileCar.getAbsolutePath();
				bCarPhoto = BitmapFactory.decodeFile(photoFileCar.getAbsolutePath());
				tv_vehicle_insurance.setBackgroundColor(Color.parseColor("#15BB18"));
				tv_vehicle_insurance.setText(getResources().getString(R.string.strUploaded));
				editor.putString("strCarPicturePath", strPicturePathCar);
				editor.commit();
			}
			
			if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
				new DriverRegister().execute();
			} else {
				networkConnectivity();
			}

		}
		
        if((requestCode == REGISTRATION_SELECT_PHOTO) && (resultCode == Activity.RESULT_OK))
        {  
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Cursor cursor = getActivity().getContentResolver().query(selectedImage,filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			strPicturePathRegistration = cursor.getString(columnIndex);
			bRegistrationPhoto = BitmapFactory.decodeFile(strPicturePathRegistration);
			tv_vehicle_registration.setBackgroundColor(Color.parseColor("#15BB18"));
			tv_vehicle_registration.setText(getResources().getString(R.string.strUploaded));
			editor.putString("strRegistrationPicturePath", strPicturePathRegistration);
			editor.commit();
        }
        
        if((requestCode == INSURANCE_SELECT_PHOTO) && (resultCode == Activity.RESULT_OK))
        {  
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Cursor cursor = getActivity().getContentResolver().query(selectedImage,filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			strPicturePathInsurance = cursor.getString(columnIndex);
			bInsurancePhoto = BitmapFactory.decodeFile(strPicturePathInsurance);
			tv_vehicle_insurance.setBackgroundColor(Color.parseColor("#15BB18"));
			tv_vehicle_insurance.setText(getResources().getString(R.string.strUploaded));
			editor.putString("strInsurancePicturePath", strPicturePathInsurance);
			editor.commit();
	    }

        if((requestCode == CAR_SELECT_PHOTO) && (resultCode == Activity.RESULT_OK))
        {  
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Cursor cursor = getActivity().getContentResolver().query(selectedImage,filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			strPicturePathCar = cursor.getString(columnIndex);
			bCarPhoto = BitmapFactory.decodeFile(strPicturePathCar);
			editor.putString("strCarPicturePath", strPicturePathCar);
			editor.commit();
			
			if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
				new DriverRegister().execute();
			} else {
				networkConnectivity();
			}
        }
        
	}
	
	private class DriverRegister extends AsyncTask<String, Integer, String> {

		String strReferralCode, strCity, strZipcode, strFullName, strSecurity, strDOB, strLicenseNumber, strDriverPicturePath,
						strPicturePathRegistration, strPicturePathInsurance, strLicensePlate, strVehicleType, strPicturePathCar;
		Bitmap bMapReg, bMapIns, bMapCar;
		
		public DriverRegister() {
			// TODO Auto-generated constructor stub
			strReferralCode = myPreferences.getString("strReferralCode", "");
			strCity = myPreferences.getString("strCity", "");
			strZipcode = myPreferences.getString("strZipcode", "");
			strFullName = myPreferences.getString("strFullName", "");
			strSecurity = myPreferences.getString("strSecurity", "");
			strDOB = myPreferences.getString("strDOB", "");
			strLicenseNumber = myPreferences.getString("strLicenseNumber", "");
			strDriverPicturePath = myPreferences.getString("strDriverPicturePath", "");
			strPicturePathRegistration = myPreferences.getString("strRegistrationPicturePath", "");
			strPicturePathInsurance = myPreferences.getString("strInsurancePicturePath", "");
			strLicensePlate = myPreferences.getString("strLicensePlate", "");
			strVehicleType = myPreferences.getString("strVehicleType", "");
			strPicturePathCar = myPreferences.getString("strCarPicturePath", "");
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			pdDialog = new ProgressDialog(getActivity());
			pdDialog.setMessage("Uploading pictures...");
			pdDialog.setCancelable(false);
			pdDialog.show();
		}
		
		@SuppressWarnings("deprecation")
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			try {
				
				String url = null;
				try {
					url = UrlGenerator.driverVehicleRegistration();
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				
				Date now = new Date(); // java.util.Date, NOT java.sql.Date or java.sql.Timestamp!
			    String format = new SimpleDateFormat("yyyyMMddHHmmss").format(now);
			    String fileName_registration = strUserId+"Reg.jpg";
			    String fileName_insurance = strUserId+"Ins.jpg";
			    String fileName_car = strUserId+"Car.jpg";
			    
			    bMapReg = BitmapFactory.decodeFile(strPicturePathRegistration);
			    ByteArrayOutputStream bosReg = new ByteArrayOutputStream();
			    bMapReg.compress(CompressFormat.JPEG, 100, bosReg);
			    
			    bMapIns = BitmapFactory.decodeFile(strPicturePathInsurance);
			    ByteArrayOutputStream bosIns = new ByteArrayOutputStream();
			    bMapIns.compress(CompressFormat.JPEG, 100, bosIns);
			    
			    bMapCar = BitmapFactory.decodeFile(strPicturePathCar);
			    ByteArrayOutputStream bosCar = new ByteArrayOutputStream();
			    bMapCar.compress(CompressFormat.JPEG, 100, bosCar);
			    
			    String strReg = uploadRegImage(bMapReg, 100, fileName_registration);
			    String strIns = uploadInsImage(bMapIns, 100, fileName_insurance);
			    String strCar = uploadCarImage(bMapCar, 100, fileName_car);
			    
			    if( (strReg.equals("") == false) && (strIns.equals("") == false) && (strCar.equals("") == false))
			    {	
			    	HttpClient httpClient = new DefaultHttpClient();
				    HttpPost httpPost = new HttpPost(url);

				    MultipartEntity entity = new MultipartEntity();
			    	
				    entity.addPart("iUserID", new StringBody(strUserId));
				    entity.addPart("fullname", new StringBody(strFullName));
				    entity.addPart("SSN", new StringBody(strSecurity));
				    entity.addPart("DOB", new StringBody(strDOB));
				    entity.addPart("licensenumber", new StringBody(strLicenseNumber));
				    entity.addPart("profileimage", new StringBody(strDriverImage));
				    entity.addPart("zipcode", new StringBody(strZipcode));
				    entity.addPart("refferal", new StringBody(strReferralCode));
				    entity.addPart("city", new StringBody(strCity));
				    entity.addPart("status", new StringBody("pending"));
				    entity.addPart("typeofcar", new StringBody(strVehicleType));
				    entity.addPart("platenumber", new StringBody(strLicensePlate));
						    
				    httpPost.setEntity(entity);
				    HttpResponse response = httpClient.execute(httpPost);
				    strResult = EntityUtils.toString(response.getEntity());
			    }
				
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
			pdDialog.dismiss();
			if(result.equals("") == false)
			{
				try {
					GsonBuilder gsonBuilder = new GsonBuilder();
					Gson gson = gsonBuilder.create();
	
					VehicleDetails vehicle = gson.fromJson(result, VehicleDetails.class);
					
					String strStatus = vehicle.status;
					String strMessage = vehicle.message;
					
					if(strStatus.equals("0") == true) {
						
						updateDriver(strUserId);
						
					} else {
						Toast.makeText(getActivity(), strMessage, Toast.LENGTH_SHORT).show();
					}
				} catch(NullPointerException e) { e.printStackTrace(); }
			}
		}
	
	}

	private String uploadRegImage(Bitmap file, int compressorQuality, String fileName) {
	    
		String url = null;
		url = UrlGenerator.uploadCarRegImage();
		String response = null;
	    HttpURLConnection conn = null;
	    try {
	        String lineEnd = "\r\n";
	        String twoHyphens = "--";
	        String boundary = "---------------------------14737809831466499882746641449";
	        URL url_reg = new URL(url);
	        conn = (HttpURLConnection) url_reg.openConnection();
	        conn.setDoInput(true);
	        conn.setDoOutput(true);
	        conn.setUseCaches(false);
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Connection", "Keep-Alive");
	        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
	        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
	        conn.setRequestProperty("uploaded_file", fileName);
	        DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
	        dos.writeBytes(lineEnd + twoHyphens + boundary + lineEnd);
	        dos.writeBytes("Content-Disposition: form-data; name=\"userfile\"; filename=\"" + fileName + "\"" + lineEnd);
	        dos.writeBytes("Content-Type: application/octet-stream" + lineEnd);
	        dos.writeBytes(lineEnd);
	        file.compress(CompressFormat.PNG, compressorQuality, dos);
	        dos.writeBytes(lineEnd);
	        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
	        dos.flush();
	        dos.close();
	        InputStream is = conn.getInputStream();
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        int bytesRead;
	        byte[] bytes = new byte[1024];
	        while ((bytesRead = is.read(bytes)) != -1) {
	            baos.write(bytes, 0, bytesRead);
	        }
	        byte[] bytesReceived = baos.toByteArray();
	        baos.close();
	        is.close();
	        response = new String(bytesReceived);
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        if (conn != null) {
	            conn.disconnect();
	        }
	    }
	    return response;
	}
	
	
	private String uploadInsImage(Bitmap file, int compressorQuality, String fileName) {
	    
		String url = null;
		url = UrlGenerator.uploadCarInsImage();
		String response = null;
	    HttpURLConnection conn = null;
	    try {
	        String lineEnd = "\r\n";
	        String twoHyphens = "--";
	        String boundary = "---------------------------14737809831466499882746641449";
	        URL url_ins = new URL(url);
	        conn = (HttpURLConnection) url_ins.openConnection();
	        conn.setDoInput(true);
	        conn.setDoOutput(true);
	        conn.setUseCaches(false);
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Connection", "Keep-Alive");
	        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
	        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
	        conn.setRequestProperty("uploaded_file", fileName);
	        DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
	        dos.writeBytes(lineEnd + twoHyphens + boundary + lineEnd);
	        dos.writeBytes("Content-Disposition: form-data; name=\"userfile\"; filename=\"" + fileName + "\"" + lineEnd);
	        dos.writeBytes("Content-Type: application/octet-stream" + lineEnd);
	        dos.writeBytes(lineEnd);
	        file.compress(CompressFormat.PNG, compressorQuality, dos);
	        dos.writeBytes(lineEnd);
	        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
	        dos.flush();
	        dos.close();
	        InputStream is = conn.getInputStream();
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        int bytesRead;
	        byte[] bytes = new byte[1024];
	        while ((bytesRead = is.read(bytes)) != -1) {
	            baos.write(bytes, 0, bytesRead);
	        }
	        byte[] bytesReceived = baos.toByteArray();
	        baos.close();
	        is.close();
	        response = new String(bytesReceived);
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        if (conn != null) {
	            conn.disconnect();
	        }
	    }
	    return response;
	}
	
	private String uploadCarImage(Bitmap file, int compressorQuality, String fileName) {
	    
		String url = null;
		url = UrlGenerator.uploadCarImage();
		String response = null;
	    HttpURLConnection conn = null;
	    try {
	        String lineEnd = "\r\n";
	        String twoHyphens = "--";
	        String boundary = "---------------------------14737809831466499882746641449";
	        URL url_car = new URL(url);
	        conn = (HttpURLConnection) url_car.openConnection();
	        conn.setDoInput(true);
	        conn.setDoOutput(true);
	        conn.setUseCaches(false);
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Connection", "Keep-Alive");
	        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
	        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
	        conn.setRequestProperty("uploaded_file", fileName);
	        DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
	        dos.writeBytes(lineEnd + twoHyphens + boundary + lineEnd);
	        dos.writeBytes("Content-Disposition: form-data; name=\"userfile\"; filename=\"" + fileName + "\"" + lineEnd);
	        dos.writeBytes("Content-Type: application/octet-stream" + lineEnd);
	        dos.writeBytes(lineEnd);
	        file.compress(CompressFormat.PNG, compressorQuality, dos);
	        dos.writeBytes(lineEnd);
	        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
	        dos.flush();
	        dos.close();
	        InputStream is = conn.getInputStream();
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        int bytesRead;
	        byte[] bytes = new byte[1024];
	        while ((bytesRead = is.read(bytes)) != -1) {
	            baos.write(bytes, 0, bytesRead);
	        }
	        byte[] bytesReceived = baos.toByteArray();
	        baos.close();
	        is.close();
	        response = new String(bytesReceived);
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        if (conn != null) {
	            conn.disconnect();
	        }
	    }
	    return response;
	}
	
	private void updateDriver(String strUserId) {
		// TODO Auto-generated method stub
		// Create AsycHttpClient object
		
		AsyncHttpClient client = new AsyncHttpClient();
		// Http Request Params Object
		RequestParams params = new RequestParams();
		
		// Show ProgressBar
		final ProgressDialog pdialog = new ProgressDialog(getActivity());
		pdialog.setMessage("Application complete...");
		pdialog.setCancelable(false);
		pdialog.show();
		// Make Http call to getusers.php
				
		String url = null;
		try {
			url = UrlGenerator.updateDriver();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		params.add("iUserID", strUserId);
		params.add("vDriverorNot", "pending");
		
		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(String response) {
				// Hide ProgressBar
				pdialog.hide();
				
				JSONObject jObject;
				try {
					jObject = new JSONObject(response);
					
					String strStatus = jObject.optString("status");
					String strMessage = jObject.optString("message");
					
					if(strStatus.equals("0") == true) {
						showDriverInactiveAlert();
					}
					
				} catch (Exception e) {e.printStackTrace();}
			}

			// When error occured
			@Override
			public void onFailure(int statusCode, Throwable error, String content) {
				// TODO Auto-generated method stub
				// Hide ProgressBar
				pdialog.hide();
				if (statusCode == 404) {
					Toast.makeText(getActivity(), "Requested resource not found", Toast.LENGTH_LONG).show();
				} else if (statusCode == 500) {
					Toast.makeText(getActivity(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
				} else {
					//networkConnectivity();
				}
			}
		});
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