package com.example.staffin;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.staffin.Interface.ApiInterface;
import com.example.staffin.Response.AddEventResponse;
import com.example.staffin.Response.EmployeeResult;
import com.example.staffin.Response.LoginResponse;
import com.example.staffin.Response.TotalEmployeeResponse;
import com.example.staffin.Retrofit.RetrofitServices;
import com.example.staffin.databinding.ActivityCreateEventBinding;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.Part;

public class CreateEventActivity extends AppCompatActivity {
    ActivityCreateEventBinding binding;

    List<String> employeesList;
    List<Integer> idsList;
    boolean[] selectedIds;
    boolean[] selectedEmployees;
    boolean atleastOne = false;
    ProgressDialog progress;
    ApiInterface apiInterface;
    HashMap<String, String> map;
    List<Integer> add_member;
    List<EmployeeResult> results;
    Uri uriImage1, uriImage2, uriImage3, uriImage4;
    String image1 = null, image2 = null, image3 = null, image4 = null;
    File file1, file2, file3, file4;
    String title, description, date, location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateEventBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        clickListeners();
    }

    private void clickListeners() {
        map = new HashMap<>();
        results = new ArrayList<>();
        add_member = new ArrayList<>();
        apiInterface = RetrofitServices.getRetrofit().create(ApiInterface.class);
        progress = new ProgressDialog(CreateEventActivity.this);
        progress.setMessage("please wait....");
        employeesList = new ArrayList<>();
        idsList = new ArrayList<>();
        if (isNetworkAvailable()) {
            progress.show();
            Call<TotalEmployeeResponse> callGetTotalEmployee = apiInterface.getTotalEmployee();
            callGetTotalEmployee.enqueue(new Callback<TotalEmployeeResponse>() {
                @Override
                public void onResponse(Call<TotalEmployeeResponse> call, Response<TotalEmployeeResponse> response) {
                    if (response.isSuccessful()) {
                        ImageView imageview[] = new ImageView[response.body().getEmployeeResult().size()];
                        results = response.body().getEmployeeResult();
                        String[] names = new String[response.body().getEmployeeResult().size()];

                        for (int i = 0; i < response.body().getEmployeeResult().size(); i++) {
                            employeesList.add(response.body().getEmployeeResult().get(i).getFullName());
                            Log.d("dfuhksdf", employeesList.get(i));
                            names[i] = employeesList.get(i);
                            idsList.add(response.body().getEmployeeResult().get(i).getId());
                            map.put(response.body().getEmployeeResult().get(i).getFullName(), response.body().getEmployeeResult().get(i).getProfileImage());
                        }

                        selectedIds = new boolean[response.body().getEmployeeResult().size()];
                        selectedEmployees = new boolean[response.body().getEmployeeResult().size()];

                        binding.btnBack.setOnClickListener(view -> {
                            finish();
                        });
                        binding.addMemberBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                AlertDialog.Builder builder = new AlertDialog.Builder(CreateEventActivity.this);
                                builder.setTitle("Add Members");
                                builder.setIcon(R.drawable.img_dp);
                                builder.setMultiChoiceItems(names, selectedEmployees, (dialog, which, isChecked) -> {
                                    selectedEmployees[which] = isChecked;
                                    selectedIds[which] = isChecked;
                                });

                                builder.setCancelable(false);

                                builder.setNeutralButton("CLEAR ALL", (dialog, which) -> {
                                    Arrays.fill(selectedEmployees, false);
                                    Arrays.fill(selectedIds, false);
                                    binding.dynamicLl.removeAllViews();
                                });
                                builder.setNegativeButton("CANCEL", (dialog, which) -> {
                                });
                                builder.setPositiveButton("Done", (dialog, which) -> {
                                    binding.dynamicLl.removeAllViews();
                                    add_member.clear();

                                    for (int i = 0; i < selectedEmployees.length; i++) {
                                        if (selectedEmployees[i]) {
                                            imageview[i] = new ImageView(CreateEventActivity.this);
                                            imageview[i].setImageResource(R.drawable.img_dp);
                                            View child = getLayoutInflater().inflate(R.layout.add_member_in_event, null);
                                            CircleImageView dp = child.findViewById(R.id.memberDp);
                                            for (EmployeeResult e : results) {
                                                if(Objects.equals(e.getId(), idsList.get(i)))
                                                {
                                                    Glide.with(CreateEventActivity.this).load(e.getProfileImage()).placeholder(R.drawable.img_dp).into(dp);
                                                }
                                            }

                                            binding.dynamicLl.addView(child);
                                        }
                                        if (selectedIds[i]) {
                                            add_member.add(idsList.get(i));
                                        }
                                    }
                                    Log.d("members Id", add_member.toString());
                                });

                                builder.create();
                                AlertDialog alertDialog = builder.create();
                                alertDialog.show();
                            }
                        });

                        progress.dismiss();
                    } else {
                        progress.dismiss();
                        Toast.makeText(CreateEventActivity.this, "some error occured", Toast.LENGTH_SHORT).show();
                        Log.d("jkdfsdf", response.message());
                    }
                }

                @Override
                public void onFailure(Call<TotalEmployeeResponse> call, Throwable t) {
                    Log.d("nfkjsf", t.getMessage());
                    progress.dismiss();
                    Toast.makeText(CreateEventActivity.this, "Unable to Load", Toast.LENGTH_SHORT).show();
                }
            });


        } else {
            Toast.makeText(this, "Internet Not Available", Toast.LENGTH_SHORT).show();
        }


        TextWatcher mTextEditorWatcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.descriptionCounterTv.setText(String.valueOf(s.length()) + "/" + "150");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

        };
        binding.descriptionEt.addTextChangedListener(mTextEditorWatcher);

        binding.createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean yes = false;
                for (boolean single : selectedEmployees) {
                    if (single) {
                        yes = true;
                    }
                }

                if (binding.titleEt.getText().toString().isEmpty()) {
                    binding.titleEt.setError("enter title");
                    binding.titleEt.requestFocus();
                } else if (!atleastOne) {
                    Toast.makeText(CreateEventActivity.this, "Upload atleast 1 image", Toast.LENGTH_SHORT).show();
                } else if (binding.locationEt.getText().toString().isEmpty()) {
                    binding.locationEt.setError("enter location");
                    binding.locationEt.requestFocus();
                } else if (binding.descriptionEt.getText().toString().isEmpty()) {
                    binding.descriptionEt.setError("enter description");
                    binding.descriptionEt.requestFocus();
                } else if (binding.dateEt.getText().toString().isEmpty()) {
                    Toast.makeText(CreateEventActivity.this, "enter date", Toast.LENGTH_SHORT).show();
                } else if (!yes) {
                    Toast.makeText(CreateEventActivity.this, "add atlease 1 member for the event", Toast.LENGTH_SHORT).show();
                } else {

//                    if (binding.second.getDrawable() == null && binding.third.getDrawable() == null && binding.fourth.getDrawable() == null) {
//                        callAddEventFunc = apiInterface.addEventFunc();
//                    } else if (binding.second.getDrawable() != null && binding.third.getDrawable() == null && binding.fourth.getDrawable() == null) {
//                        callAddEventFunc = apiInterface.addEventFunc();
//                    } else if (binding.second.getDrawable() != null && binding.third.getDrawable() != null && binding.fourth.getDrawable() == null) {
//                        callAddEventFunc = apiInterface.addEventFunc();
//                    } else if (binding.second.getDrawable() != null && binding.third.getDrawable() != null && binding.fourth.getDrawable() != null) {
//                        callAddEventFunc = apiInterface.addEventFunc();
//                    }

                    progress.show();

                    file1 = new File(image1);
                    RequestBody image11 = RequestBody.create(MediaType.parse("image/*"), file1);
                    MultipartBody.Part image = MultipartBody.Part.createFormData("image", file1.getName(), image11);
                    Call<LoginResponse> callAddEventFunc = null;

                    title = binding.titleEt.getText().toString();
                    description = binding.descriptionEt.getText().toString();
                    location = binding.locationEt.getText().toString();
                    date = binding.dateEt.getText().toString();

                    RequestBody tit = RequestBody.create(MediaType.parse("text/plain"), title);
                    RequestBody des = RequestBody.create(MediaType.parse("text/plain"), description);
                    RequestBody loc = RequestBody.create(MediaType.parse("text/plain"), location);
                    RequestBody dat = RequestBody.create(MediaType.parse("text/plain"), date);

                    String integerListString = TextUtils.join(",", add_member);
                    RequestBody add_memb = RequestBody.create(MediaType.parse("text/plain"), integerListString);

                    if (binding.second.getDrawable() == null && binding.third.getDrawable() == null && binding.fourth.getDrawable() == null) {

                        callAddEventFunc = apiInterface.addEventFunc(image, null, null, null, tit, loc, des, dat, add_memb);

                    } else if (binding.second.getDrawable() != null && binding.third.getDrawable() == null && binding.fourth.getDrawable() == null) {

                        file2 = new File(image2);
                        RequestBody image22 = RequestBody.create(MediaType.parse("image/*"), file2);
                        MultipartBody.Part image1 = MultipartBody.Part.createFormData("image1", file2.getName(), image22);

                        callAddEventFunc = apiInterface.addEventFunc(image, image1, null, null, tit, loc, des, dat, add_memb);

                    } else if (binding.second.getDrawable() != null && binding.third.getDrawable() != null && binding.fourth.getDrawable() == null) {

                        file2 = new File(image2);
                        RequestBody image22 = RequestBody.create(MediaType.parse("image/*"), file2);
                        MultipartBody.Part image1 = MultipartBody.Part.createFormData("image1", file2.getName(), image22);
                        file3 = new File(image3);
                        RequestBody image33 = RequestBody.create(MediaType.parse("image/*"), file3);
                        MultipartBody.Part image2 = MultipartBody.Part.createFormData("image2", file3.getName(), image33);

                        callAddEventFunc = apiInterface.addEventFunc(image, image1, image2, null, tit, loc, des, dat, add_memb);

                    } else if (binding.second.getDrawable() != null && binding.third.getDrawable() != null && binding.fourth.getDrawable() != null) {

                        file2 = new File(image2);
                        RequestBody image22 = RequestBody.create(MediaType.parse("image/*"), file2);
                        MultipartBody.Part image1 = MultipartBody.Part.createFormData("image1", file2.getName(), image22);
                        file3 = new File(image3);
                        RequestBody image33 = RequestBody.create(MediaType.parse("image/*"), file3);
                        MultipartBody.Part image2 = MultipartBody.Part.createFormData("image2", file3.getName(), image33);
                        file4 = new File(image4);
                        RequestBody image44 = RequestBody.create(MediaType.parse("image/*"), file4);
                        MultipartBody.Part image3 = MultipartBody.Part.createFormData("image3", file4.getName(), image44);
                        callAddEventFunc = apiInterface.addEventFunc(image, image1, image2, image3, tit, loc, des, dat, add_memb);

                    }

                    callAddEventFunc.enqueue(new Callback<LoginResponse>() {
                        @Override
                        public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                            if (response.isSuccessful()) {
                                progress.dismiss();
                                Toast.makeText(CreateEventActivity.this, "Event Added Successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(CreateEventActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Log.d("jfsdfsd", response.message());
                                Toast.makeText(CreateEventActivity.this, "error occured", Toast.LENGTH_SHORT).show();
                                progress.dismiss();
                            }
                        }

                        @Override
                        public void onFailure(Call<LoginResponse> call, Throwable t) {
                            progress.dismiss();
                            Toast.makeText(CreateEventActivity.this, "failure", Toast.LENGTH_SHORT).show();
                            Log.d("jkdfsdf", t.getMessage());
                        }
                    });


                }
            }
        });

        binding.uploadImg.setOnClickListener(v -> {
            Intent imgIntent = new Intent(Intent.ACTION_PICK);
            imgIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(imgIntent, 100);
        });

        binding.img1.setOnClickListener(v -> {
            if (binding.first.getTag().equals("filled") && binding.second.getTag().equals("filled") && binding.third.getTag().equals("filled") && binding.fourth.getTag().equals("filled")) {
                binding.first.setImageDrawable(binding.second.getDrawable());
                binding.second.setImageDrawable(binding.third.getDrawable());
                binding.third.setImageDrawable(binding.fourth.getDrawable());
                binding.fourth.setTag("empty");
                binding.fourth.setImageDrawable(null);
            } else if (binding.first.getTag().equals("filled") && binding.second.getTag().equals("filled") && binding.third.getTag().equals("filled") && binding.fourth.getTag().equals("empty")) {
                binding.first.setImageDrawable(binding.second.getDrawable());
                binding.second.setImageDrawable(binding.third.getDrawable());
                binding.third.setTag("empty");
                binding.third.setImageDrawable(null);
            } else if (binding.first.getTag().equals("filled") && binding.second.getTag().equals("filled") && binding.third.getTag().equals("empty") && binding.fourth.getTag().equals("empty")) {
                binding.first.setImageDrawable(binding.second.getDrawable());
                binding.second.setTag("empty");
                binding.second.setImageDrawable(null);
            } else if (binding.first.getTag().equals("filled") && binding.second.getTag().equals("empty") && binding.third.getTag().equals("empty") && binding.fourth.getTag().equals("empty")) {
                binding.first.setTag("empty");
                binding.first.setImageDrawable(null);
            }

            if (binding.first.getDrawable() == null && binding.second.getDrawable() == null && binding.third.getDrawable() == null && binding.fourth.getDrawable() == null) {
                atleastOne = false;
            }
        });

        binding.img2.setOnClickListener(v -> {
            if (binding.second.getTag().equals("filled") && binding.third.getTag().equals("filled") && binding.fourth.getTag().equals("filled")) {
                binding.second.setImageDrawable(binding.third.getDrawable());
                binding.third.setImageDrawable(binding.fourth.getDrawable());
                binding.fourth.setTag("empty");
                binding.fourth.setImageDrawable(null);
            } else if (binding.second.getTag().equals("filled") && binding.third.getTag().equals("filled") && binding.fourth.getTag().equals("empty")) {
                binding.second.setImageDrawable(binding.third.getDrawable());
                binding.third.setTag("empty");
                binding.third.setImageDrawable(null);
            } else if (binding.second.getTag().equals("filled") && binding.third.getTag().equals("empty") && binding.fourth.getTag().equals("empty")) {
                binding.second.setTag("empty");
                binding.second.setImageDrawable(null);
            }
        });

        binding.img3.setOnClickListener(v -> {
            if (binding.third.getTag().equals("filled") && binding.fourth.getTag().equals("filled")) {
                binding.third.setImageDrawable(binding.fourth.getDrawable());
                binding.fourth.setTag("empty");
                binding.fourth.setImageDrawable(null);
            } else if (binding.third.getTag().equals("filled") && binding.fourth.getTag().equals("empty")) {
                binding.third.setTag("empty");
                binding.third.setImageDrawable(null);
            }
        });

        binding.img4.setOnClickListener(v -> {
            if (binding.fourth.getTag().equals("filled")) {
                binding.fourth.setTag("empty");
                binding.fourth.setImageDrawable(null);
            }
        });
        binding.dateEt.setOnClickListener(v -> {

            final Calendar c = Calendar.getInstance();

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(CreateEventActivity.this,
                    (view, year1, monthOfYear, dayOfMonth) -> {
                        //
                        binding.dateEt.setText(year1 + "-" + (monthOfYear + 1) + "-" + dayOfMonth);
                    },
                    year, month, day);
            datePickerDialog.show();
        });
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 100) {
            if (binding.first.getTag().equals("empty")) {
                binding.first.setImageURI(data.getData());
                uriImage1 = data.getData();
                image1 = getRealPathFromURI(uriImage1);
                atleastOne = true;
                binding.first.setTag("filled");
            } else if (binding.second.getTag().equals("empty")) {
                binding.second.setImageURI(data.getData());
                uriImage2 = data.getData();
                image2 = getRealPathFromURI(uriImage2);
                binding.second.setTag("filled");
            } else if (binding.third.getTag().equals("empty")) {
                binding.third.setImageURI(data.getData());
                uriImage3 = data.getData();
                image3 = getRealPathFromURI(uriImage3);
                binding.third.setTag("filled");
            } else if (binding.fourth.getTag().equals("empty")) {
                binding.fourth.setImageURI(data.getData());
                uriImage4 = data.getData();
                image4 = getRealPathFromURI(uriImage4);
                binding.fourth.setTag("filled");
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBooleanArray("selecteds", selectedEmployees);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        selectedEmployees = savedInstanceState.getBooleanArray("selecteds");
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager != null ? connectivityManager.getActiveNetworkInfo() : null;
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}