package com.example.screenshoteasy;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class PolicyDialog extends DialogFragment {



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_app_policy, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView txtViewPolicy = view.findViewById(R.id.textViewPolicy);
        Button btnAccept = view.findViewById(R.id.buttonAccept);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            txtViewPolicy.setText(Html.fromHtml(policy, Html.FROM_HTML_MODE_COMPACT));
        } else {
            txtViewPolicy.setText(Html.fromHtml(policy));
        }
        txtViewPolicy.setClickable(true);
        txtViewPolicy.setMovementMethod(LinkMovementMethod.getInstance());

        btnAccept.setOnClickListener(v -> dismiss());
    }

    String policy = "<span style=\"font-size: 24px;\"><strong>Privacy Policy</strong></span><br/><br/>" +
            "\n" +
            "We built the Screenshot Easy - Best Capture app as a Free app. This SERVICE is provided byus at no cost and is intended for use as is.<br/><br/>" +
            "\n" +
            "This page is used to inform visitors regarding our policies with the collection, use, and disclosure of Personal Information if anyone decided to use our Service.<br/><br/>" +
            "\n" +
            "If you choose to use our Service, then you agree to the collection and use of information in relation to this policy. The Personal Information that we collect is used for providing and improving the Service. We will not use or share your information with anyone except as described in this Privacy Policy.<br/><br/>" +
            "\n" +
            "&nbsp;\n" +
            "\n" +
            "The terms used in this Privacy Policy have the same meanings as in our Terms and Conditions, which is accessible at Screenshot Easy - Best Capture unless otherwise defined in this Privacy Policy.<br/><br/>" +
            "\n" +
            "<strong>Information Collection and Use</strong><br/><br/>" +
            "\n" +
            "For a betting experience, while using our Service, we may require you to provide us with certain personally identifiable information. The information that we request will be retained by us and used as described in this privacy policy.<br/><br/>" +
            "\n" +
            "The app does use third-party services that may collect information used to identify you.<br/><br/>" +
            "\n" +
            "Link to the privacy policy of third-party service providers used by the app<br/><br/>" +
            "\n" +
            "<a href=\"https://policies.google.com/\">Google Play Services</a><br/><br/>" +
            "\n" +
            "<strong>Log Data </strong><br/><br/>" +
            "\n" +
            "We would like to inform you that whenever you use our Service, in the case of an error in the app we collect data and information (through third-party products) on your phone called Log Data. This Log Data may include information such as your device Internet Protocol (\"IP\") address, device name, operating system version, the configuration of the app when utilizing our Service, the time and date of your use of the Service, and other statistics.<br/><br/>" +
            "\n" +
            "<strong>Cookies</strong><br/><br/>" +
            "\n" +
            "Cookies are files with a small amount of data that are commonly used as anonymous unique identifiers. These are sent to your browser from the websites that you visit and are stored on your device's internal memory.<br/><br/>" +
            "\n" +
            "This Service does not use these \"cookies\" explicitly. However, the app may use third-party code and libraries that use \"cookies\" to collect information for improving their services. You have the option to either accept or refuse these cookies and know when a cookie is being sent to your device. If you choose to refuse our cookies, you may not be able to use some portions of this Service.<br/><br/>" +
            "\n" +
            "<strong>Service Providers</strong><br/><br/>" +
            "\n" +
            "We may employ third-party companies and individuals due to the following reasons:<br/><br/>" +
            "\n" +
            "* To facilitate our Service;<br/><br/>" +
            "\n" +
            "* To provide the Service on our behalf;<br/><br/>" +
            "\n" +
            "* To perform Service-related services;<br/><br/>" +
            "\n" +
            "* To assist us in analyzing how our Service is used.<br/><br/>" +
            "\n" +
            "We would like to inform users of this Service that these third parties have access to your Personal Information. The reason is to perform the tasks assigned to them on our behalf. However, they are obligated not to disclose or use the information for any other purpose.<br/><br/>" +
            "\n" +
            "&nbsp;";
}
