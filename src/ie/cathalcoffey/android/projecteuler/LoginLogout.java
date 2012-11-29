package ie.cathalcoffey.android.projecteuler;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

import ie.cathalcoffey.android.projecteuler.PageFragment.SolveOperation;
import ie.cathalcoffey.android.projecteuler.ProjectEulerClient.EulerProblem;
import ie.cathalcoffey.android.projecteuler.ProjectEulerClient.EulerProfile;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginLogout extends SherlockFragmentActivity implements LoginDialogFragment.NoticeDialogListener
{
	FragmentActivity fragmentActivity;
	Context context;
    
	public class LoginOperation extends AsyncTask<String, Void, String> 
	{
		  LoginDialogFragment dialog;
		  String progressMsg;
		  boolean success;
		  boolean completed;
		   
		  public LoginOperation(FragmentActivity fragmentActivity)
		  {
			  dialog = new LoginDialogFragment();
			  dialog.setCancelable(false);
			  dialog.show(fragmentActivity.getSupportFragmentManager(), "");
		  }
		
	      @Override
	      protected String doInBackground(String... params) 
	      {
	    	    success = false;
	    	  
	    	    String username = params[0];
				String password = params[1];
				
			    ProjectEulerClient pec = MyApplication.pec;
			    if(MyApplication.pec == null)
			    {
			    	MyApplication.pec = new ProjectEulerClient();
			    	pec = MyApplication.pec;
			    }
			    
			    try 
			    {
					if(pec.login(username, password))
					{
						EulerProfile ep = pec.getProfile();
						
						MyApplication.prefEditor.putString("username", username);
						MyApplication.prefEditor.putString("password", password);
						MyApplication.prefEditor.putString("alias", ep.alias);
						MyApplication.prefEditor.putString("country", ep.country);
						MyApplication.prefEditor.putString("language", ep.language);
						MyApplication.prefEditor.putString("level", ep.level);
						MyApplication.prefEditor.putString("solved", ep.solved);
		    	        
						this.progressMsg = "Login successful";
						publishProgress();
				    	
						try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); };
						  
						this.progressMsg = "Syncing data";
						publishProgress();
						
						try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); };
						
						ArrayList<EulerProblem> problems = pec.getProblems();
					
						MyApplication.myDbHelper.updateProblems(pec, problems, false);	
						
						success = true;
						completed = true;
						
				        this.progressMsg = "Finished";
						publishProgress();
						
						try { Thread.sleep(1000); } catch (InterruptedException e) { e.printStackTrace(); };
					}
					
					else
					{
						this.progressMsg = pec.getError();
						completed = true;
						
						publishProgress();
					}
				} 
			    
			    catch (ClientProtocolException e) 
			    {
			        this.progressMsg = "Unable to connect to projecteuler.net, please check your internet connection.";
					publishProgress();
				} 
			    
			    catch (IOException e) 
			    {
			    	 this.progressMsg = "Unable to connect to projecteuler.net, please check your internet connection.";
					 publishProgress();
				}
			    
	          return null;
	      }      

	      @Override
	      protected void onPostExecute(String result) 
	      {               	    	  

	      }

	      @Override
	      protected void onPreExecute() 
	      {

	      }

	      @Override
	      protected void onProgressUpdate(Void... values) 
	      {
	    	  try
	    	  {
		    	  if(dialog != null)
		    	  {  
			    	  MyApplication.login_opt.progressMsg = progressMsg;
			    	  
			    	  dialog.setMessage(progressMsg);
			    	  
			    	  if(completed)
			    		  dialog.completed();
		    	  }
	    	  }
	    	  
	    	  catch(Exception e)
	    	  {
	    		  Log.e("Exception", e.getMessage());
	    	  }
	      }
	}
	
	public boolean onOptionsItemSelected(MenuItem item) 
    {
    	  if (item.getItemId() == android.R.id.home) 
    	  {
              finish();
              overridePendingTransition(0, 0);
              
              return true;
          }
    	  
    	  return true;
    }
	
	@Override
	public void onBackPressed() 
	{
    	if(MyApplication.login_opt != null)
		{
		    MyApplication.login_opt.cancel(true);
		    MyApplication.login_opt = null;
		}
    	
	    this.finish();
	    overridePendingTransition(0, 0);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);

	    fragmentActivity = this;
	    getSupportActionBar().setDisplayShowTitleEnabled(false);
	    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	    
	    context = this;
	    
	    if(MyApplication.settings == null)
	        MyApplication.settings = getSharedPreferences("euler", MODE_PRIVATE);
	    
	    if(MyApplication.prefEditor == null)
	    	MyApplication.prefEditor = MyApplication.settings.edit();

        if(MyApplication.settings.contains("username"))
        {
        	setContentView(R.layout.logout);
        	
        	TextView tv = (TextView)findViewById(R.id.textView1);
        	tv.setText(MyApplication.settings.getString("username", "unknown"));
        	
        	Button b = (Button)findViewById(R.id.button1);
    	    b.setOnClickListener
    	    (
    	    		new OnClickListener()
    	    		{
    					@Override
    					public void onClick(View v) 
    					{
    						MyApplication.myDbHelper.updateSolved();	
    						
    						MyApplication.prefEditor.clear();
    						MyApplication.prefEditor.commit();
    						
    						finish();
    						overridePendingTransition(0, 0);
    					}
    				}
    	    );
        }
        
        else
        {
        	setContentView(R.layout.login);
        	
        	Button b = (Button)findViewById(R.id.button1);
    	    b.setOnClickListener
    	    (
    	    		new OnClickListener()
    	    		{
    					@Override
    					public void onClick(View v) 
    					{
    						EditText et1 = (EditText)findViewById(R.id.editText1);
    						EditText et2 = (EditText)findViewById(R.id.editText2);
    						
    						String username = et1.getText().toString();
    						String password = et2.getText().toString();
    						
    						if(MyApplication.login_opt == null)
    						{
    						    MyApplication.login_opt = new LoginOperation(fragmentActivity);
    						    MyApplication.login_opt.execute(new String[]{username, password});
    						}
    					}
    				}
    	    );
        }
	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog) 
	{
		MyApplication.prefEditor.commit();
		
		if(MyApplication.login_opt != null)
		{
		    MyApplication.login_opt.cancel(true);
		    MyApplication.login_opt = null;
		}
		
		finish();
		overridePendingTransition(0, 0);
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) 
	{
		if(MyApplication.login_opt != null)
		{
		    MyApplication.login_opt.cancel(true);
		    MyApplication.login_opt = null;
		}
	}


	@Override
	public void solved() {
		// TODO Auto-generated method stub
		
	}
}