/*
 * Copyright 2015 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.webbluetoothcg.bletestperipheral;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import static android.bluetooth.BluetoothGatt.GATT_FAILURE;
import static android.bluetooth.BluetoothGatt.GATT_SUCCESS;
import static android.content.Context.WIFI_SERVICE;


public class WifiServiceFragment extends ServiceFragment {

  private static final UUID WIFI_SERVICE_UUID = UUID
      .fromString("f1111237-0a17-48e6-b842-aa814d891ace");

  private static final UUID WIFI_SSID_UUID = UUID
          .fromString("f1111237-0a18-48e6-b842-aa814d891ace");
  private static final String WIFI_SSID_DESCRIPTION = "The ssid of Wifi network";

  private static final UUID WIFI_PASSWORD_UUID = UUID
          .fromString("f1111237-0a19-48e6-b842-aa814d891ace");
  private static final String WIFI_PASSWORD_DESCRIPTION = "The password of Wifi network";

  private static final UUID WIFI_TYPE_UUID = UUID
          .fromString("f1111237-0a20-48e6-b842-aa814d891ace");
  private static final String WIFI_TYPE_DESCRIPTION = "WPA/WPA2";

  private static final UUID WIFI_STATUS_UUID = UUID
          .fromString("f1111237-0a18-48e6-b842-aa814d891ace");
  private static final String WIFI_STATUS_DESCRIPTION = "Not Connected/Connecting/Connected/Error";

  private static final UUID WIFI_COMMAND_UUID = UUID
          .fromString("f1111237-0b17-48e6-b842-aa814d891ace");
  private static final String WIFI_COMMAND_DESCRIPTION = "Connect/Reset";



  private ServiceFragmentDelegate mDelegate;

  private TextView mWifiStatusTextView;
  private TextView mWifiSSIDTextView;
  private TextView mWifiPASSTextView;
  private TextView mWifiTypeTextView;
  private TextView mWifiCommandTextView;


  // GATT
  private BluetoothGattService mWifiService;
  private BluetoothGattCharacteristic mWifiSSIDCharacteristic;
  private BluetoothGattCharacteristic mWifiPasswordCharacteristic;
  private BluetoothGattCharacteristic mWifiTypeCharacteristic;
  private BluetoothGattCharacteristic mWifiStatusCharacteristic;
  private BluetoothGattCharacteristic mWifiCommandCharacteristic;

  public WifiServiceFragment() {
    mWifiSSIDCharacteristic =
        new BluetoothGattCharacteristic(WIFI_SSID_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
            BluetoothGattCharacteristic.PERMISSION_WRITE);
    mWifiSSIDCharacteristic.addDescriptor(
        Peripheral.getClientCharacteristicConfigurationDescriptor());
    mWifiSSIDCharacteristic.addDescriptor(
        Peripheral.getCharacteristicUserDescriptionDescriptor(WIFI_SSID_DESCRIPTION));


    mWifiPasswordCharacteristic =
            new BluetoothGattCharacteristic(WIFI_PASSWORD_UUID,
                    BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE);
    mWifiPasswordCharacteristic.addDescriptor(
            Peripheral.getClientCharacteristicConfigurationDescriptor());
    mWifiPasswordCharacteristic.addDescriptor(
            Peripheral.getCharacteristicUserDescriptionDescriptor(WIFI_PASSWORD_DESCRIPTION));

    mWifiTypeCharacteristic =
            new BluetoothGattCharacteristic(WIFI_TYPE_UUID,
                    BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE);
    mWifiTypeCharacteristic.addDescriptor(
            Peripheral.getClientCharacteristicConfigurationDescriptor());
    mWifiTypeCharacteristic.addDescriptor(
            Peripheral.getCharacteristicUserDescriptionDescriptor(WIFI_TYPE_DESCRIPTION));

    mWifiStatusCharacteristic =
            new BluetoothGattCharacteristic(WIFI_STATUS_UUID,
                    BluetoothGattCharacteristic.PROPERTY_READ | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PERMISSION_READ);
    mWifiStatusCharacteristic.addDescriptor(
            Peripheral.getClientCharacteristicConfigurationDescriptor());
    mWifiStatusCharacteristic.addDescriptor(
            Peripheral.getCharacteristicUserDescriptionDescriptor(WIFI_STATUS_DESCRIPTION));

    mWifiCommandCharacteristic =
            new BluetoothGattCharacteristic(WIFI_COMMAND_UUID,
                    BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE);
    mWifiCommandCharacteristic.addDescriptor(
            Peripheral.getClientCharacteristicConfigurationDescriptor());
    mWifiCommandCharacteristic.addDescriptor(
            Peripheral.getCharacteristicUserDescriptionDescriptor(WIFI_COMMAND_DESCRIPTION));


    mWifiService = new BluetoothGattService(WIFI_SERVICE_UUID,
        BluetoothGattService.SERVICE_TYPE_PRIMARY);
    mWifiService.addCharacteristic(mWifiSSIDCharacteristic);
    mWifiService.addCharacteristic(mWifiPasswordCharacteristic);
    mWifiService.addCharacteristic(mWifiTypeCharacteristic);
    mWifiService.addCharacteristic(mWifiCommandCharacteristic);
    mWifiService.addCharacteristic(mWifiStatusCharacteristic);
  }

  // Lifecycle callbacks
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_wifi, container, false);

    mWifiStatusTextView = (TextView) view.findViewById(R.id.label_wifiStatus);
    mWifiSSIDTextView = (TextView) view.findViewById(R.id.label_wifiSSID);
    mWifiPASSTextView = (TextView) view.findViewById(R.id.label_wifiPASS);
    mWifiTypeTextView = (TextView) view.findViewById(R.id.label_wifiType);
    mWifiCommandTextView = (TextView) view.findViewById(R.id.label_wifiCommand);



    return view;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mDelegate = (ServiceFragmentDelegate) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString()
          + " must implement ServiceFragmentDelegate");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mDelegate = null;
  }

  public BluetoothGattService getBluetoothGattService() {
    return mWifiService;
  }

  @Override
  public ParcelUuid getServiceUUID() {
    return new ParcelUuid(WIFI_SERVICE_UUID);
  }



  @Override
  public void notificationsEnabled(BluetoothGattCharacteristic characteristic, boolean indicate) {
    if (characteristic.getUuid() != WIFI_STATUS_UUID) {
      return;
    }
    if (indicate) {
      return;
    }
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(getActivity(), R.string.notificationsEnabled, Toast.LENGTH_SHORT)
            .show();
      }
    });
  }

  @Override
  public void notificationsDisabled(BluetoothGattCharacteristic characteristic) {
    if (characteristic.getUuid() != WIFI_STATUS_UUID) {
      return;
    }
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(getActivity(), R.string.notificationsNotEnabled, Toast.LENGTH_SHORT)
            .show();
      }
    });
  }

  private void connectWIFI() throws Exception {
    // https://stackoverflow.com/questions/8818290/how-do-i-connect-to-a-specific-wi-fi-network-in-android-programmatically

        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiConfiguration config = new WifiConfiguration();
        String ssid = new String(mWifiSSIDCharacteristic.getValue(), "UTF-8");
        String password = new String(mWifiPasswordCharacteristic.getValue(), "UTF-8");

        config.SSID = "\""+ssid+"\"";
        config.preSharedKey = "\""+password+"\"";

        int networkId = wifiManager.addNetwork(config);

        if (networkId != -1) {
            wifiManager.disconnect();
            wifiManager.enableNetwork(networkId, true);
        }
  }

  @Override
  public int writeCharacteristic(BluetoothGattCharacteristic characteristic, int offset, byte[] value) {
     if (characteristic.setValue(value)) {
       try {
         UUID uuid = characteristic.getUuid();
         String text = new String(value, "UTF-8");
         if (uuid.equals(WIFI_PASSWORD_UUID)) {
           mWifiPASSTextView.setText(text);
         } else if (uuid.equals(WIFI_SSID_UUID)) {
           mWifiSSIDTextView.setText(text);
         } else if (uuid.equals(WIFI_STATUS_UUID)) {
           mWifiStatusTextView.setText(text);
         } else if (uuid.equals(WIFI_COMMAND_UUID)) {
           mWifiCommandTextView.setText(text);
         } else if (uuid.equals(WIFI_TYPE_UUID)) {
           mWifiTypeTextView.setText(text);
         }
       } catch (UnsupportedEncodingException e) {
         e.printStackTrace();
       }
       return GATT_SUCCESS;
     }
     return GATT_FAILURE;
  }
}
