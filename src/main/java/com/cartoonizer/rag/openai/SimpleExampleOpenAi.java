package com.cartoonizer.rag.openai;

import com.cartoonizer.rag.shared.utils.FragmentQuestionsProcessor;

public class SimpleExampleOpenAi {

//    public static final String PREFIX = "InfoDispatch";
//    public static final String LAYOUT = "fragment_info_dispatch.xml";
    public static String PREFIX = "ContactCentral";
    public static String LAYOUT = "fragment_contact_central.xml";
    public static String[] PREFIXES = {
            "Dashboard",
            "ContactCentral",
            "Home",
            "InfoDispatch",
            "DispatchReceived",
            "LoginUser",
        };
        public static String[] LAYOUTS = {
            "fragment_dashboard.xml",
            "fragment_contact_central.xml",
            "fragment_home.xml",
            "fragment_info_dispatch.xml",
            "fragment_dispatch_received.xml",
            "fragment_login_user.xml",
        };
    public static void main(String[] args) {
        for (int i = 0; i < LAYOUTS.length; i++) {
            LAYOUT = LAYOUTS[i];
            PREFIX = PREFIXES[i];
            FragmentQuestionsProcessor.askQuestions();
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
            }
        }
    }

}
