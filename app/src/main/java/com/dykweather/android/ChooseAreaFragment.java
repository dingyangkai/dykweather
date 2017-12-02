package com.dykweather.android;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dykweather.android.db.City;
import com.dykweather.android.db.County;
import com.dykweather.android.db.Province;
import com.dykweather.android.gson.Weather;
import com.dykweather.android.util.HttpUtil;
import com.dykweather.android.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by 1 on 2017/11/24.
 */

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;   //省标记
    public static final int LEVEL_CITY = 1;    //市标记
    public static final int LEVEL_COUNTY = 2;   //县标记
    private ProgressDialog progressDialog;   //进度条
    private TextView titleText;       //标题
    private Button backButton;       //返回按钮
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    private List<Province> provinceList;   //省列表
    private List<City> cityList;  //市列表
    private List<County> countyList;  //县列表

    private Province selectProvince;  //选中的省份
    private City selectCity;  //选中的城市
    private int currentLevel;  //当前选中的级别


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     View view=inflater.inflate(R.layout.choose_area,container,false);
     titleText=view.findViewById(R.id.title_text);
     backButton=view.findViewById(R.id.back_button);
     listView=view.findViewById(R.id.list_view);
     adapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,dataList);
     listView.setAdapter(adapter);
     return view;
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);
      listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              if (currentLevel==LEVEL_PROVINCE){
                  selectProvince=provinceList.get(position);
                  queryCities();

          }else if (currentLevel==LEVEL_CITY){
                  selectCity=cityList.get(position);
                  queryCounties();
              }
                  else if (currentLevel==LEVEL_COUNTY){
              String weatherId=countyList.get(position).getWeatherId();
              if (getActivity() instanceof MainActivity){


              Intent intent=new Intent(getActivity(), WeatherActivity.class);
              intent.putExtra("weather_id",weatherId);
              startActivity(intent);
              getActivity().finish();}
              else if (getActivity() instanceof WeatherActivity){
                   WeatherActivity activity= (WeatherActivity) getActivity();
                   activity.drawerLayout.closeDrawers();
                   activity.swipeRefresh.setRefreshing(true);
                   activity.requestWeather(weatherId);

              }
              }
              }
      });



        //设置按钮监听事件
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvince();
                }
            }
        });
        queryProvince();
    }

    /**
     * 查询所有的省，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryProvince() {
        titleText.setText("中国");
        //隐藏返回按钮
        backButton.setVisibility(View.GONE);
        //先从数据库中查找数据
        provinceList = DataSupport.findAll(Province.class);
        //大于0，说明数据库中有数据
        if (provinceList.size() > 0) {
            //重新设置适配器，更新数据
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            //数据库中没有数据，从网络中获取
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    /**
     * 查询选中省中的所有的市，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCities() {
        titleText.setText(selectProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?", String.valueOf(selectProvince.getId())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    /**
     * 查询选中市中的所有的县，优先从数据库查询，如果没有查询到再去服务器上查询
     */
    private void queryCounties() {
        titleText.setText(selectCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid=?", String.valueOf(selectCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectProvince.getProvinceCode();
            int cityCode = selectCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "county");
        }
    }

    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     *
     * @param address
     * @param type
     */
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseText);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseText,
                            selectProvince.getId());
                } else if ("county".equals(type)) {
                    result = Utility.handleCountyResponse(responseText,
                            selectCity.getId());
                }
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("porvince".equals(type)) {
                                queryProvince();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("county".equals(type)) {
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    /**
     * 显示进度条
     */

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("加载中...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度条
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

}
