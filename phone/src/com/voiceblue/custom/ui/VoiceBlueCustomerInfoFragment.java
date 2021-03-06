package com.voiceblue.custom.ui;

import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.voiceblue.phone.R;
import com.voiceblue.custom.config.AccessInformation;
import com.voiceblue.custom.config.ConfigDownloaderCallbacks;
import com.voiceblue.custom.config.ConfigDownloaderResult;
import com.voiceblue.custom.config.ConfigDownloaderTask;
import com.voiceblue.custom.config.VoiceBlueAccount;
import com.voiceblue.custom.config.VoiceBlueURL;
import com.voiceblue.custom.customer.VoiceBlueCustomerInfo;
import com.voiceblue.phone.api.SipConfigManager;
import com.voiceblue.phone.api.SipManager;
import com.voiceblue.phone.api.SipUri;

public class VoiceBlueCustomerInfoFragment extends SherlockListFragment implements ConfigDownloaderCallbacks {
		
	private final String TAG = "VoiceBlueCustomerInfoFragment";
	
	private TextView mTxtCustomerName;
	private TextView mTxtCompany;
	private TextView mTxtAccountType;
	private TextView mTxtCredit;
	//private TextView mTxtExpirationDate;
	private ImageView mIVFlag;
	private Button mBtnLogin;
	private Button mBtnCallCustomerService;
	private Button mBtnBuyCredit;
	private View mProgressContainer;
	private View mPrepaidLayout;
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setHasOptionsMenu(true);        
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);              
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	
    	View v = inflater.inflate(R.layout.customer_info, container, false);
    	
    	mTxtCustomerName = (TextView) v.findViewById(R.id.txtCustomerName);
    	mTxtCompany = (TextView) v.findViewById(R.id.txtCompany);
    	mTxtAccountType = (TextView) v.findViewById(R.id.txtAccountType);
    	mTxtCredit = (TextView) v.findViewById(R.id.txtCreditLeft);
    	//mTxtExpirationDate = (TextView) v.findViewById(R.id.txtExpirationDate);
    	mIVFlag = (ImageView) v.findViewById(R.id.ivFlag);
    	
    	mBtnLogin = (Button) v.findViewById(R.id.btnLoginToAccount);
    	mBtnLogin.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String myAccURL = SipConfigManager.getPreferenceStringValue(getActivity(), VoiceBlueAccount.MY_ACCOUNT_KEY);;
				
				if (myAccURL != null && !myAccURL.equals(""))
					goToURL(myAccURL);
				else
					Toast.makeText(getActivity(), getString(R.string.voiceblue_config_value_missing), Toast.LENGTH_LONG).show();
			}
		});    	    
    	
    	mBtnCallCustomerService = (Button) v.findViewById(R.id.btnCallCustomerService);
    	mBtnCallCustomerService.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String URI = SipConfigManager.getPreferenceStringValue(getActivity(), VoiceBlueAccount.CUSTOMER_CARE_KEY);;
				
				if (URI != null && !URI.equals(""))
					placeCall(URI);
				else
					Toast.makeText(getActivity(), getString(R.string.voiceblue_config_value_missing), Toast.LENGTH_LONG).show();
			}
		});
    	
    	mBtnBuyCredit = (Button) v.findViewById(R.id.btnBuyCredit);
    	mBtnBuyCredit.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String buyCreditURL = SipConfigManager.getPreferenceStringValue(getActivity(), VoiceBlueAccount.TOP_UP_URL_KEY);
				
				if (buyCreditURL != null && !buyCreditURL.equals(""))
					goToURL(buyCreditURL);
				else
					Toast.makeText(getActivity(), getString(R.string.voiceblue_config_value_missing), Toast.LENGTH_LONG).show();
			}
		});
    	
    	mProgressContainer = v.findViewById(R.id.progressContainer);    	
    	mPrepaidLayout = v.findViewById(R.id.layoutPrepaid);
    	
    	new ConfigDownloaderTask(this).execute(new AccessInformation(VoiceBlueURL.CUSTOMER_INFO)
    												.loadCredentialFromConfigurationFile(v.getContext()));
        return v;
    }

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPreDownloading() {		
	}

	@Override
	public void onDownladingCancelled() {
		showResult("Download cancelled");		
	}

	@Override
	public void onConfigDownloaded(ConfigDownloaderResult result) {
		try {
			
			if (!result.operationSuccessful())
				throw new Exception("Couldn't retrieve customer's info");											
			
			JSONObject jsonResult = result.getObjectResult();
			
			if (jsonResult == null)
				throw new NullPointerException("Json result is null");
			
			VoiceBlueCustomerInfo customerInfo = new VoiceBlueCustomerInfo();
			
			customerInfo.setName(jsonResult.getString("customer_name"))
						.setCompany(jsonResult.getString("company"))
						.setAccountType(jsonResult.getString("account_type"))
						.setCountry(jsonResult.getString("country"))
						.setCredit(jsonResult.getDouble("credit"))
						.setCreditCurrency(jsonResult.getString("credit_currency"))
						.setStatus(jsonResult.getString("status"))
						.setExpirationDate(jsonResult.getString("exp_date"));
			
			renderResult(customerInfo);					
		}
		catch(Exception e) {
			e.printStackTrace();
			if (e.getMessage() != null) {
				Log.e(TAG, e.getMessage());			
				showResult(e.getMessage());
				return;
			}
			
			showResult("Unable to retrive customer's info");
		}
	}
	
	private void renderResult(VoiceBlueCustomerInfo customerInfo) {											
		mProgressContainer.setVisibility(View.GONE);
		
		mTxtCustomerName.setText(customerInfo.getName());
		mTxtCompany.setText(customerInfo.getCompany());
		
		mTxtAccountType.setText((customerInfo.isActive() ? 
										getString(R.string.voiceblue_active) +  " | " :  
										"Not Active | " )
								+ customerInfo.getAccountTypeDescription(getActivity()));
		
		if (customerInfo.getFlagResource() == -1)
			mIVFlag.setVisibility(View.GONE);
		
		mIVFlag.setImageResource(customerInfo.getFlagResource());
		
		if (!customerInfo.isPrepaid()) {
			mPrepaidLayout.setVisibility(View.GONE);
			return;
		}
		
		mTxtCredit.setText(customerInfo.getCreditCurrency() 
							+ " " 
							+ Double.toString(customerInfo.getCredit()));					
	}
	
	private void showResult(String message) {
		ProgressBar pb = (ProgressBar) mProgressContainer.findViewById(R.id.progressContainerPB);
		TextView txt = (TextView) mProgressContainer.findViewById(R.id.progressContainerText);
		
		pb.setVisibility(View.GONE);
		txt.setText(message);
	}
	
	private void goToURL(String url) {		
		Intent i = new Intent(Intent.ACTION_VIEW);		
		i.setData(Uri.parse(url));
		startActivity(i);
	}
	
	 private void placeCall(String uri) {	        
		 
		 Intent it = new Intent(Intent.ACTION_CALL);
		 it.setData(SipUri.forgeSipUri(SipManager.PROTOCOL_CSIP, uri));
	     it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	     
	     getActivity().startActivity(it);
	 }

}
