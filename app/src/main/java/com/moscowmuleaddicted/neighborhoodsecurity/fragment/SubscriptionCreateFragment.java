package com.moscowmuleaddicted.neighborhoodsecurity.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.model.LatLng;
import com.moscowmuleaddicted.neighborhoodsecurity.R;

import org.apache.commons.lang3.math.NumberUtils;

import static com.moscowmuleaddicted.neighborhoodsecurity.controller.Constants.MAX_LATITUDE;
import static com.moscowmuleaddicted.neighborhoodsecurity.controller.Constants.MAX_LONGITUDE;
import static com.moscowmuleaddicted.neighborhoodsecurity.controller.Constants.MAX_RADIUS;
import static com.moscowmuleaddicted.neighborhoodsecurity.controller.Constants.MIN_LATITUDE;
import static com.moscowmuleaddicted.neighborhoodsecurity.controller.Constants.MIN_LONGITUDE;
import static com.moscowmuleaddicted.neighborhoodsecurity.controller.Constants.MIN_RADIUS;
import static com.moscowmuleaddicted.neighborhoodsecurity.controller.Constants.RC_PERMISSION_POSITION;

/**
 * Fragment containing the fields required for the creation of a new {@link com.moscowmuleaddicted.neighborhoodsecurity.model.Subscription}
 *
 * @author Simone Ripamonti
 * @version 2
 */
public class SubscriptionCreateFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    /**
     * Logger's TAG
     */
    private static final String TAG = "SubscriptionCreateFrag";
    /**
     * Edit text latitude
     */
    private EditText etLatitude;
    /**
     * Edit text longitude
     */
    private EditText etLongitude;
    /**
     * Input layout latitude
     */
    private TextInputLayout ilLatitude;
    /**
     * Input layout longitude
     */
    private TextInputLayout ilLongitude;
    /**
     * Textview current radius
     */
    private TextView tvSeekbarCurValue;
    /**
     * Seekbar radius
     */
    private SeekBar sbRadius;
    /**
     * Radio group address and coordinates
     */
    private RadioGroup radioGroup;
    /**
     * Radio button address
     */
    private RadioButton rbAddress;
    /**
     * Image view showing a compass
     */
    private ImageView ivGetPosition;
    /**
     * Google Api Client to obtain current position
     */
    private GoogleApiClient mGoogleApiClient;
    /**
     * Device last known location
     */
    private Location mLastLocation;
    /**
     * Google Places API autocomplete fragment
     */
    private SupportPlaceAutocompleteFragment placeAutocompleteFragment;
    /**
     * Fragment interaction listener
     */
    private OnFragmentInteractionListener mListener;
    /**
     * Constructor
     */
    public SubscriptionCreateFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_subscription_create, container, false);


        etLatitude = (EditText) view.findViewById(R.id.input_latitude);
        etLongitude = (EditText) view.findViewById(R.id.input_longitude);
        ilLatitude = (TextInputLayout) view.findViewById(R.id.input_layout_latitude);
        ilLongitude = (TextInputLayout) view.findViewById(R.id.input_layout_longitude);
        ivGetPosition = (ImageView) view.findViewById(R.id.subscription_get_position);

        tvSeekbarCurValue = (TextView) view.findViewById(R.id.seekbar_title_value);

        sbRadius = (SeekBar) view.findViewById(R.id.seekbar_radius);

        radioGroup = (RadioGroup) view.findViewById(R.id.radio_group_subscription);

        rbAddress = (RadioButton) view.findViewById(R.id.radio_address_sub);

        placeAutocompleteFragment = (SupportPlaceAutocompleteFragment) getChildFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                LinearLayout addressGroup = (LinearLayout) view.findViewById(R.id.layout_address_group);
                RelativeLayout coordinatesGroup = (RelativeLayout) view.findViewById(R.id.layout_coordinates_group);

                if (rbAddress.isChecked()) {
                    addressGroup.setVisibility(View.VISIBLE);
                    coordinatesGroup.setVisibility(View.GONE);
                } else {
                    addressGroup.setVisibility(View.GONE);
                    coordinatesGroup.setVisibility(View.VISIBLE);
                }
            }
        });

        sbRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 1) {
                    tvSeekbarCurValue.setText(String.format(getString(R.string.metres_singular), progress));
                } else {
                    tvSeekbarCurValue.setText(String.format(getString(R.string.metres_plural), progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sbRadius.setProgress(500);

        placeAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.i(TAG, "Place: " + place.getName());
                LatLng ll = place.getLatLng();
                etLatitude.setText(String.valueOf(ll.latitude));
                etLongitude.setText(String.valueOf(ll.longitude));
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);


            }
        });
        placeAutocompleteFragment.setHint(getString(R.string.hint_address));

        ivGetPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mLastLocation != null){
                    radioGroup.check(R.id.radio_coordinates_sub);
                    etLatitude.setText(String.valueOf(mLastLocation.getLatitude()));
                    etLongitude.setText(String.valueOf(mLastLocation.getLongitude()));
                    Toast.makeText(getContext(), String.format(getString(R.string.last_known_location_ok), (int)mLastLocation.getAccuracy()), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), R.string.last_known_location_fail, Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // request permissions for accessing location, requires SDK >= 23 (marshmellow)
                Log.d(TAG, "onConnected: prompting user to allow location permissions");
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, RC_PERMISSION_POSITION);
            } else {
                Log.w(TAG, "onConnected: SDK version is too low (" + Build.VERSION.SDK_INT + ") to ask permissions at runtime");
                Toast.makeText(getContext(), "Give location permission to allow application know events around you", Toast.LENGTH_LONG).show();
            }

        } else {
            // permissions already granted
            Log.d(TAG, "onConnected: location permission already granted, requesting last known position");
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == RC_PERMISSION_POSITION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: location permission granted, requesting last known position");
                //noinspection MissingPermission
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            } else {
                Log.d(TAG, "onRequestPermissionsResult: location permission not granted");
            }
        }
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Fragment listener interface
     */
    public interface OnFragmentInteractionListener {
    }

    /**
     * Radius getter
     * @return
     */
    public int getRadius() {
        return sbRadius.getProgress();
    }

    /**
     * Latitude getter
     * @return
     */
    public Double getLatitude() {
        return NumberUtils.toDouble(etLatitude.getText().toString(), -1000d);
    }

    /**
     * Longitude getter
     * @return
     */
    public Double getLongitude() {
        return NumberUtils.toDouble(etLongitude.getText().toString(), -1000d);
    }

    /**
     * Show errors in the various fields
     */
    public void showErrors() {
        if (rbAddress.isChecked()) {
            Toast.makeText(getContext(), getString(R.string.msg_insert_valid_place), Toast.LENGTH_SHORT).show();
            ilLatitude.setError(null);
            ilLongitude.setError(null);
        } else {
            Double latitude = NumberUtils.toDouble(etLatitude.getText().toString(), -1000);
            Double longitude = NumberUtils.toDouble(etLongitude.getText().toString(), -1000);
            if (etLatitude.getText().length() == 0 || latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
                ilLatitude.setError(getString(R.string.msg_insert_valid_latitude));
            } else {
                ilLatitude.setError(null);
            }

            if (etLongitude.getText().length() == 0 || longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
                ilLongitude.setError(getString(R.string.msg_insert_valid_longitude));
            } else {
                ilLongitude.setError(null);
            }

            if(sbRadius.getProgress() < MIN_RADIUS || sbRadius.getProgress()>MAX_RADIUS){
                // cheated?
                Toast.makeText(getContext(), getString(R.string.msg_insert_valid_radius), Toast.LENGTH_SHORT).show();
                sbRadius.setProgress(500);
            }
        }
    }

    /**
     * Manually set location according to coordinates
     * @param lat latitude
     * @param lon longitude
     */
    public void setLocation(double lat, double lon) {
        radioGroup.check(R.id.radio_coordinates_sub);
        etLatitude.setText(String.valueOf(lat));
        etLongitude.setText(String.valueOf(lon));
    }


}
