package com.example.home;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ReminderItemsAdapter extends RecyclerView.Adapter<ReminderItemsAdapter.ReminderItemsHolder> {

    ArrayList<String> dt, db, dTime, dDate;
    SharedPreferences sharedPreferences;
    Context context;
    private OnEditClickListener listener;
    Double val;

    ReminderItemsAdapter(ArrayList<String> dt, ArrayList<String> db, ArrayList<String> dTime, ArrayList<String> dDate, Context context) {
        this.dt = dt;
        this.db = db;
        this.dTime = dTime;
        this.dDate = dDate;
        sharedPreferences = context.getSharedPreferences(MainActivity.email, Context.MODE_PRIVATE);
        this.context = context;
        listener = (OnEditClickListener) context;
    }


    public interface OnEditClickListener {
        void onEditClick(String str);
    }


    public class ReminderItemsHolder extends RecyclerView.ViewHolder {

        TextView title, body, time, date,distance;
        ImageView edit, delete;

        public ReminderItemsHolder(@NonNull final View itemView, final SharedPreferences sharedPreferences1, final OnEditClickListener listener) {
            super(itemView);
            title = itemView.findViewById(R.id.titlePlace);
            body = itemView.findViewById(R.id.body);
            time = itemView.findViewById(R.id.rem_Time);
            date = itemView.findViewById(R.id.rem_Date);
            distance=itemView.findViewById(R.id.distance);
            edit = itemView.findViewById(R.id.edit);
            delete = itemView.findViewById(R.id.delete);
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteReminder(sharedPreferences1, getAdapterPosition());

                    listener.onEditClick(title.getText().toString());

                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    deleteReminder(sharedPreferences1, getAdapterPosition());
                }
            });
        }
    }


    @NonNull
    @Override
    public ReminderItemsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_list, parent, false);
        return new ReminderItemsHolder(view, sharedPreferences, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ReminderItemsHolder holder, final int position) {
        final String name = dt.get(position);
        String content = db.get(position);
        String timeRem = dTime.get(position);
        String dateRem = dDate.get(position);
        holder.title.setText(name);
        holder.body.setText(content);
        holder.time.setText(timeRem);
        holder.date.setText(dateRem);

        Double lat=MainActivity.lat;
        Double lng=MainActivity.lng;
        LatLng latLng=getLatLngFromAddress(name);
        if(lat!=0.0 && lng!=0.0){
            val=MainActivity.checkDistance(latLng.latitude,lat,latLng.longitude,lng);
            if(val<1000.0) {
                int dis=(int)MainActivity.checkDistance(latLng.latitude,lat,latLng.longitude,lng);
                String tx= String.valueOf(dis);
                tx+=" m away";
                holder.distance.setText(tx);
            }
            else{
                val/=1000;
                String tx=String.valueOf(val);
                tx=tx.substring(0,Math.min(4,tx.length()));
                tx+=" kms away";
                holder.distance.setText(tx);
            }
        }
        else{
            holder.distance.setText("Turn on Gps to know distance");
        }

    }

    @Override
    public int getItemCount() {
        return dt.size();
    }


    public void deleteReminder(SharedPreferences sharedPreferences1, int pos) {
        int i = pos;

        int request_Code = 0;
        String address = dt.get(i);
        String body = db.get(i);
        for (int j = 0; j < address.length(); j++)
            request_Code += ((int) address.charAt(j) * (int) address.charAt(j));
        if (body != null) {
            for (int j = 0; j < body.length(); j++) {
                request_Code += ((int) body.charAt(j) * (int) body.charAt(j));
            }
        }

        dt.remove(i);
        db.remove(i);
        dTime.remove(i);
        dDate.remove(i);


        SharedPreferences.Editor editor = sharedPreferences1.edit();
        Gson gson1 = new Gson();
        String json = gson1.toJson(dt);
        String json5 = gson1.toJson(db);
        String json6 = gson1.toJson(dTime);
        String json7 = gson1.toJson(dDate);
        editor.putString("dataTitle", json);
        editor.putString("dataBody", json5);
        editor.putString("dataTime", json6);
        editor.putString("dataDate", json7);
        editor.apply();

        notifyItemRemoved(pos);
        notifyItemRangeChanged(pos, dt.size());


        request_Code %= Integer.MAX_VALUE;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,request_Code,new Intent(context,MyBroadcastReceiver.class),0);
        AlarmManager alarmManager= (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public LatLng getLatLngFromAddress(String address) {

        Geocoder geocoder = new Geocoder(context);
        List<Address> addressList;

        try {
            addressList = geocoder.getFromLocationName(address, 1);
            if (addressList != null) {
                Address singleaddress = addressList.get(0);
                LatLng latLng = new LatLng(singleaddress.getLatitude(), singleaddress.getLongitude());
                return latLng;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

}
