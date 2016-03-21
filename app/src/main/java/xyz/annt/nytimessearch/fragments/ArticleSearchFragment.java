package xyz.annt.nytimessearch.fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.annt.nytimessearch.R;

/**
 * Created by annt on 3/21/16.
 */
public class ArticleSearchFragment extends DialogFragment {
    private long searchBeginTimestamp = 0;

    @Bind(R.id.tvBeginDateValue) TextView tvBeginDateValue;
    @Bind(R.id.ivRemoveBeginDate) ImageView ivRemoveBeginDate;
    @Bind(R.id.spSort) Spinner spSort;
    @Bind(R.id.cbNewsDeskArts) CheckBox cbNewsDeskArts;
    @Bind(R.id.cbNewsDeskFashionStyle) CheckBox cbNewsDeskFashionStyle;
    @Bind(R.id.cbNewsDeskSports) CheckBox cbNewsDeskSports;

    public interface ArticleSearchFragmentListener {
        void onSaveSettings(long searchBeginTimestamp, String searchSort, ArrayList<String> searchNewsDesk);
    }

    public ArticleSearchFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_article_search, null);
        ButterKnife.bind(this, view);

        // Get arguments
        Bundle args = getArguments();

        searchBeginTimestamp = args.getLong("searchBeginTimestamp");
        if (0 < searchBeginTimestamp) {
            tvBeginDateValue.setText(getDateString(searchBeginTimestamp, "MM-dd-yyyy"));
            ivRemoveBeginDate.setVisibility(View.VISIBLE);
        } else {
            ivRemoveBeginDate.setVisibility(View.INVISIBLE);
        }

        ArrayAdapter<CharSequence> searchSortAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.search_sort_array, android.R.layout.simple_spinner_item);
        searchSortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSort.setAdapter(searchSortAdapter);
        String searchSort = args.getString("searchSort");
        if (null != searchSort) {
            spSort.setSelection(searchSortAdapter.getPosition(searchSort));
        }

        ArrayList<String> searchNewsDesk = args.getStringArrayList("searchNewsDesk");
        if (null != searchNewsDesk) {
            cbNewsDeskArts.setChecked(searchNewsDesk.contains("Arts"));
            cbNewsDeskFashionStyle.setChecked(searchNewsDesk.contains("Fashion & Style"));
            cbNewsDeskSports.setChecked(searchNewsDesk.contains("Sports"));
        }

        builder.setTitle(R.string.search_settings)
                .setView(view)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        final AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onPositiveClick(dialog);
                    }
                });

        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    public void onPositiveClick(Dialog dialog) {
        dialog.dismiss();

        ArticleSearchFragmentListener listener = (ArticleSearchFragmentListener) getActivity();

        String searchSort = spSort.getSelectedItem().toString();

        ArrayList<String> searchNewsDesk = new ArrayList<>();
        if (cbNewsDeskArts.isChecked()) {
            searchNewsDesk.add("Arts");
        }
        if (cbNewsDeskFashionStyle.isChecked()) {
            searchNewsDesk.add("Fashion & Style");
        }
        if (cbNewsDeskSports.isChecked()) {
            searchNewsDesk.add("Sports");
        }

        listener.onSaveSettings(searchBeginTimestamp, searchSort, searchNewsDesk);

        dismiss();
    }

    @OnClick(R.id.tvBeginDateValue)
    public void onBeginDateClick() {
        showDatePickerDialog();
    }

    public void showDatePickerDialog() {
        DatePickerFragment newFragment = new DatePickerFragment() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                setBeginTimestamp(year, monthOfYear, dayOfMonth);
            }
        };
        Bundle args = new Bundle();
        args.putLong("timeInMillis", searchBeginTimestamp);
        newFragment.setArguments(args);
        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
    }

    private void setBeginTimestamp(int year, int monthOfYear, int dayOfMonth) {
        searchBeginTimestamp = ymdToTimeInMillis(year, monthOfYear, dayOfMonth);
        tvBeginDateValue.setText(getDateString(searchBeginTimestamp, "MM-dd-yyyy"));
        ivRemoveBeginDate.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.ivRemoveBeginDate)
    public void removeBeginTimestamp() {
        searchBeginTimestamp = 0;
        tvBeginDateValue.setText("");
        ivRemoveBeginDate.setVisibility(View.INVISIBLE);
    }

    private long ymdToTimeInMillis(int year, int monthOfYear, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, monthOfYear);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        return c.getTimeInMillis();
    }

    private String getDateString(long timeStamp, String format){
        if (timeStamp > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            Date date = (new Date(timeStamp));
            return sdf.format(date);
        }

        return "";
    }
}
