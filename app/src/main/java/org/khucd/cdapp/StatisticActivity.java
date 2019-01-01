package org.khucd.cdapp;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;

public class StatisticActivity extends AppCompatActivity implements  ListViewBtnAdapter.ListBtnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistic);

        ListView listview;
        ListViewBtnAdapter adapter;
        ArrayList<ListViewItem> items = new ArrayList<ListViewItem>();

        initiate(items);

        adapter = new ListViewBtnAdapter(this, R.layout.listview_item, items, this);

        listview = (ListView) findViewById(R.id.StatisticView);
        listview.setAdapter(adapter);
    }

    public boolean initiate(ArrayList<ListViewItem> list){
        ListViewItem item;
        int i;

        if(list == null){
            list = new ArrayList<ListViewItem>();
        }
        i = 1;

        item = new ListViewItem();
        item.setTitleStr("한국 성폭력 위기센터");
        item.setSubscription("서울특별시 강남구 남부순환로 359길 31, 1층 102호");
        item.setSubscription2("성폭력 피해자를 대상으로 피해상담, 법률상담 및 의료검진을 지원해주는 센터입니다.");
        list.add(item);
        i++;

        item = new ListViewItem();
        item.setTitleStr("전국 범죄 피해자 지원 연합회");
        item.setSubscription("서울특별시 서초구 반포대로 157번지 대검찰청 107호");
        item.setSubscription2("범죄 피해자를 대상으로 치료비, 생계비 등을 지원하고 취업을 연계시켜주는 전국 단위 연합회입니다.");
        list.add(item);
        i++;

        item = new ListViewItem();
        item.setTitleStr("해바라기 아동센터");
        item.setSubscription("서울시 마포구 신수동 63-14 (구)프라자 7층");
        item.setSubscription2("아동 성폭력 센터로, 정신과 진료 및 법률지원을 받을 수 있는 센터입니다.");
        list.add(item);
        i++;

        item = new ListViewItem();
        item.setTitleStr("학교폭력 피해자 지원센터");
        item.setSubscription("경상북도 안동시 축제장길 20");
        item.setSubscription2("학교 폭력 피해자 상담 및 피해 지원 도움을 받을 수 있는 센터입니다.");
        list.add(item);
        i++;

        item = new ListViewItem();
        item.setTitleStr("교통사고 피해자 구호센터");
        item.setSubscription("서울특별시 종로구 적선동 156번지 광화문플래티넘 615호");
        item.setSubscription2("보험소비자연맹을 운영하며, 재해사고 보상과 교통사고 피해자 구호를 수행하는 센터입니다.");
        list.add(item);
        i++;

        item = new ListViewItem();
        item.setTitleStr("한국여성인권진흥원");
        item.setSubscription("서울특별시 중구 서소문로 50 센트럴플레이스 3층");
        item.setSubscription2("성매매, 성폭력, 가정폭력 예방 및 피해 지원 도움을 주는 센터입니다.");
        list.add(item);
        i++;
        return true;
    }
    public void onListBtnClick(int position){
        if(position == 0){
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:02-883-9284"));
            startActivity(intent);
        }
        else if(position == 1){
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:02-596-1259"));
            startActivity(intent);
        }
        else if(position == 2){
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:02-3274-1375"));
            startActivity(intent);
        }
        else if(position == 3){
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:054-850-1075"));
            startActivity(intent);
        }
        else if(position == 4){
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:1577-0095"));
            startActivity(intent);
        }
        else if(position == 5){
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:1366"));
            startActivity(intent);
        }
    }
}
