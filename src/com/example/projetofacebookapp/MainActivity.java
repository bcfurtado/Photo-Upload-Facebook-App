package com.example.projetofacebookapp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookActivity;
import com.facebook.HttpMethod;
import com.facebook.Request.Callback;
import com.facebook.RequestAsyncTask;
import com.facebook.SessionState;
import com.facebook.GraphUser;
import com.facebook.Request;
import com.facebook.Response;


public class MainActivity extends FacebookActivity {

	private static final List<String> PERMISSIONS = 
			Arrays.asList("publish_actions","publish_stream","photo_upload",
					"offline_access", "user_photos", "publish_checkins" );
    
	private static final int ESCOLHER_IMAGEM = 1;
	
	private Button buttonEscolherImagem;
	private Button buttonEnviarImagem;
	private EditText editTextTituloImagem;
	private ImageView imagemViewPreview;
	private Uri imagemUri;
    
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		buttonEscolherImagem = (Button)findViewById(R.id.buttonEscolherImagem);
		buttonEscolherImagem.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				escolherImagem();
			}
		});
		
		buttonEnviarImagem = (Button)findViewById(R.id.buttonEnviar);
		buttonEnviarImagem.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				enviarImagem();
			}
		});
		
		editTextTituloImagem = (EditText)findViewById(R.id.editTextTituloImagem);
		
		imagemViewPreview = (ImageView)findViewById(R.id.imageViewPreview);
		
		this.openSessionForPublish("398382490232122", PERMISSIONS);
	}

	protected void escolherImagem() {
		Intent it = new Intent(Intent.ACTION_PICK);
		it.setType("image/*");
		startActivityForResult(it, ESCOLHER_IMAGEM);
	}
	
	protected void enviarImagem()  {

		if ( imagemUri != null ) {
			byte[] data = null;
			try {
			    ContentResolver cr = this.getContentResolver();
			    InputStream fis = cr.openInputStream(imagemUri);
			    Bitmap bi = BitmapFactory.decodeStream(fis);
			    ByteArrayOutputStream baos = new ByteArrayOutputStream();
			    bi.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			    data = baos.toByteArray();
			} catch (FileNotFoundException e) {
			    e.printStackTrace();
			}
			
			Bundle parametros = new Bundle();
			parametros.putString("name", editTextTituloImagem.getText().toString());
			parametros.putByteArray("picture", data);		
			
			Request request = new Request(getSession(),"me/photos", parametros, HttpMethod.POST, new Callback() {
				
				public void onCompleted(Response response) {
					onCompleteSendPhoto(response);
				}
			});
			
			Toast.makeText(getApplicationContext(), "Enviando Imagem", Toast.LENGTH_SHORT).show();		
			RequestAsyncTask task =  new RequestAsyncTask(request);
			task.execute();
		} else {
			Toast.makeText(this, "Selecione uma imagem.", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch (requestCode) {
		case ESCOLHER_IMAGEM:
			
			if ( resultCode == RESULT_OK ) {
				Uri imagemUri = data.getData();
				if (imagemUri != null) {
					this.imagemUri = imagemUri;
					imagemViewPreview.setImageURI(imagemUri);			
				}
			}
			break;
		default:
			break;
		}
	}
	
	private void onCompleteSendPhoto(Response response){	
		Toast.makeText(getApplicationContext(), "Imagem Enviada", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	protected void onSessionStateChange(SessionState state, Exception exception) {
		// user has either logged in or not ...
		if (state.isOpened()) {
			// make request to the /me API
			Request request = Request.newMeRequest(this.getSession(),
					new Request.GraphUserCallback() {
						// callback after Graph API response with user object
						public void onCompleted(GraphUser user, Response response) {
							if (user != null) {
								TextView welcome = (TextView) findViewById(R.id.hello);
								welcome.setText("Olá " + user.getName() + "!");
							}
						}
					});
			Request.executeBatchAsync(request);
		}
	}
}
