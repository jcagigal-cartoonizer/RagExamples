package com.cartoonizer.rag.shared.utils;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class GeneralAnswerProcessor extends ReadFile {
    public static void main(String[] args) {
        for (int i = 0; i < LAYOUTS.length; i++) {
            LAYOUT = LAYOUTS[i];
            PREFIX = PREFIXES[i];
            if (!ONLY_THIS.isEmpty() && !PREFIX.equals(ONLY_THIS)) {
                continue;
            }
//            FragmentAnswerProcessor.processAll();
            FILE_NAME = PREFIX + "Fragment.txt";
            String answerPath = "./output-files/" + FILE_NAME;
            String processedAnswerPath = "./processed-files/processed-" + FILE_NAME;
            ReadFile reader = new GeneralAnswerProcessor(answerPath, processedAnswerPath);
            reader.load();
        }
    }

    public static String PREFIX = "ContactCentral";
    public static String SUFFIX = "Fragment";
    public static String LAYOUT = "fragment_contact_central.xml";
    public static String[] PREFIXES = {
            "Dashboard",
            "ContactCentral",
            "Home",
            "InfoDispatch",
            "DispatchReceived",
            "LoginUser",
            "About",
            "AddAmount",
            "BluetoothDiscovery",
            "ChangeDriverPin",
            "ChangePasswordRedSys",
            "ChangeUserPassword",
        
                "ChooseOption",
                "ClosedPartial",
                "CropImage",
                "DestinationMap",
                "DeviceSettings",                
                "FixedPrice",
                "GPSTest",
                "InformationMessage",
                "LegalText",
                "LightsTest",
                "LoginDriver",
                "LoginUserRedSys",
                "LoginUser",
                "MacroZoning",
        
                "MeetingSign",
                "MessageDetail",
                "Messages",
                "OfflineInvoice",
                "OnlineInvoice",
                "OnTrip",
                "OpenPartial",
                "PaymentMonei",
                "Payment",
                "PendingTrips",
                
        };
        public static String[] LAYOUTS = {
            "fragment_dashboard.xml",
            "fragment_contact_central.xml",
            "fragment_home.xml",
            "fragment_info_dispatch.xml",
            "fragment_dispatch_received.xml",
            "fragment_login_user.xml",
            "fragment_about.xml",
            "fragment_add_amount.xml",
            "fragment_bluetooth_discovery.xml",
            "fragment_change_driver_pin.xml",
            "fragment_change_password_red_sys.xml",
            "fragment_change_user_password.xml",
            
                "fragment_choose_option.xml",
                "fragment_closed_partial.xml",
                "fragment_crop_image_view.xml",
                "fragment_destination_map.xml",
                "fragment_device_settings.xml",                
                "fragment_fixed_price_map.xml",
                "fragment_gps_test.xml",
                "fragment_information_messages.xml",
                "fragment_legal_text.xml",
                "fragment_lights_test.xml",
                "fragment_login_driver.xml",
                "fragment_login_user_red_sys.xml",
                "fragment_login_user.xml",
                "fragment_macro_zoning.xml",
            
                "fragment_meeting_sign.xml",
                "fragment_message_detail.xml",
                "fragment_message.xml",
                "fragment_offline_invoice.xml",
                "fragment_online_invoice.xml",
                "fragment_on_trip.xml",
                "fragment_open_partial.xml",
                "fragment_payment_monei.xml",
                "fragment_payment.xml",
                "fragment_pending_trips.xml",
        };
    public static String ONLY_THIS = "";
    public static String FILE_NAME = PREFIX + "Fragment.txt";
    public static IGeneralBlocks iface;
    private boolean printAlways;
    private String processedAnswerPath;
    private PrintWriter writer;
    public HashMap<String, String> blockFiles = new HashMap<>();
    public HashMap<String, String> blocks = new HashMap<>();
    public String[] blockFilesList = new String[0];
    public String[] packagesArray = new String[0];
    public String[] blocksList = new String[0];
    public String[] packagesList = new String[0];
    private HashMap<String, String> importsMap = new HashMap<>();
    private HashMap<String, String> lines = new HashMap<>();
    private HashMap<Integer, String> orderedLines = new HashMap<>();
    private int numBlock = 0;
    private int numLines = 0;
    private boolean firstImport = false;
    private boolean hasImports = true;
    private String fileNameToUse = "";
    private String previousLine = "";
    private String blockLine = "";
    public static IGeneralBlocks getIface() {
        if ("InfoDispatch".equals(PREFIX)) {
            return new InfoDispatchBlocks();
        } else if ("ContactCentral".equals(PREFIX)) {
            return new ContactCentralBlocks();
        } else if ("Dashboard".equals(PREFIX)) {
            return new DashboardBlocks();
        } else if ("LoginUser".equals(PREFIX)) {
            return new LoginUserBlocks();
        } else if ("Shared".equals(PREFIX) && "CommonDialog".equals(SUFFIX)) {
            return new CommonDialogBlocks();
        } else {
            return new GeneralBlocks();
        }
    }
    public GeneralAnswerProcessor(String pathOrigen, String processedAnswerPath) {
        super(pathOrigen);
        System.out.println("*** GeneralAnswerProcessor pathOrigen = " + pathOrigen);
        this.processedAnswerPath = processedAnswerPath;
        if (processedAnswerPath != null && !processedAnswerPath.isEmpty()) {
            try {
                writer = new PrintWriter(processedAnswerPath);
            } catch (Exception ex) {
            }
        }
        iface = getIface();
        numBlock = 0;
        numLines = 0;
        hasImports = true;
        importsMap = new HashMap<>();
        lines = new HashMap<>();
        blocks = new HashMap<>();
        orderedLines = new HashMap<>();
        blockFiles = new HashMap<>();
        firstImport = false;
        fileNameToUse = "";
        blockLine = "";
    }
    public static boolean shouldIgnore(String line) {
        line = line.trim();
        return (line.startsWith("This ") || line.startsWith("You ") || line.startsWith("If you") || 
                line.startsWith("Single ") || line.contains("Note: ") || line.startsWith("- ") || 
                line.startsWith("Only one ") || line.startsWith("Use ") || line.startsWith("It keeps") || 
                line.startsWith("One flow") || line.startsWith("It’s ") || line.startsWith("Helper") ||
                line.startsWith("Since ") || line.startsWith("The screen") ||
                line.startsWith("For navigation") || line.startsWith("import ifac.td.taxi.ui.screen." + PREFIX.toLowerCase()) ||
                line.startsWith("Below is a") || line.startsWith("I’m ") || line.startsWith("It ") || line.startsWith("And "));

    }
    public HashMap<String, String> getBlocks() {
        return blocks;
    }

    public HashMap<String, String> getBlockFiles() {
        return blockFiles;
    }

    @Override
    public void processLine(String line) {
        if (line.trim().isEmpty()) {
            return;
        }
        if (line.trim().startsWith("package ")) {
            fileNameToUse = "";
            printAlways = true;
//            return;
        }
        if (line.trim().startsWith("import ")) {
            printAlways = true;
        }
        if (line.contains("ifac.td.taxi.ui.screen.state.ComposeButtonState")
                || line.contains("ifac.td.taxi.ui.screen.state.MessageUiState")) {
            return;
        }
        if (line.trim().startsWith("import ")) {
            if (line.contains("import ifac.td.taxi.repository.connections.service.model.ShortBreakStatus")) {
//                System.out.println("*** firstImport = " + firstImport + " block = " + blockLine + " importsMap.get(line) = " + importsMap.get(line));
            }
            if (!firstImport) {
                firstImport = true;
                hasImports = true;
                numBlock++;
                blockLine = "// # Block " + numLines + "-" + numBlock + ": " + line;
//                System.out.println("*** blockLine " + blockLine);
                numLines++;
                orderedLines.put(numLines, blockLine);
                lines.put(blockLine, blockLine);
                blocks.put(blockLine, blockLine);
            }
            importsMap.put(line, line);
        } else {
            if (!hasImports) {
                System.out.println("*** NO IMPORTS IN " + processedAnswerPath);
                hasImports = true;
                numBlock++;
                blockLine = "// # Block " + numLines + "-" + numBlock + ": " + line;
                System.out.println("*** blockLine " + blockLine);
                numLines++;
                orderedLines.put(numLines, blockLine);
                lines.put(blockLine, blockLine);
                blocks.put(blockLine, blockLine);
            }
                if (fileNameToUse.isEmpty()) {
                    if (line.contains("class ")) {
                        fileNameToUse = extractFileNameFromClass(line);
    //                    System.out.println("*** fileNameToUse " + fileNameToUse + " in " + line + " blockLine " + blockLine);
                    blockFiles.put(blockLine, fileNameToUse);
                    } else if ("@Composable".equals(previousLine) && line.startsWith("fun ")) {
                        fileNameToUse = extractFileNameFromFun(line);
    //                    System.out.println("*** fileNameToUse " + fileNameToUse + " in " + line + " blockLine " + blockLine);
                    blockFiles.put(blockLine, fileNameToUse);
                    } else if (line.trim().startsWith("object ")) {
                        fileNameToUse = extractFileNameFromObject(line);
    //                    System.out.println("*** fileNameToUse " + fileNameToUse + " in " + line + " blockLine " + blockLine);
                    blockFiles.put(blockLine, fileNameToUse);
                    }
                }
            firstImport = false;
        }
        if (!shouldIgnore(line)) {
            numLines++;
            orderedLines.put(numLines, line);
            lines.put(line, line);
        }
        previousLine = line;
    }
    private void doProcessLine(String line) {
        if (line.trim().isEmpty()) {
            return;
        }
        if (shouldIgnore(line)) {
            return;
        }
        if (line.contains("ifac.td.taxi.ui.screen.state.ComposeButtonState")
                || line.contains("ifac.td.taxi.ui.screen.state.MessageUiState")) {
            return;
        }
            if (iface == null) {
                iface = getIface();
            }
        if (line.trim().startsWith("import ") || line.trim().startsWith("#")) {
            printAlways = true;
        }
        if (iface.isEndTag(line)) {
            printAlways = false;
            if (super.origen.getName().contains("ContactCentralFragment")) {
                System.out.println("-> printAlways = " + printAlways + " isEndTag = " + iface.isEndTag(line) + " " + line);
            }
        }
        if (!printAlways) {
            if (super.origen.getName().contains("ContactCentralFragment")) {
                System.out.println("-> printAlways = " + printAlways + " isEndTag = " + iface.isEndTag(line) + " " + line);
            }
            return;
        }
        if (line.contains("DialogButtonSpec")) {
            line = line.replaceAll(Pattern.quote("DialogButtonSpec"), PREFIX +  "DialogButtonSpec");
        }
        if (line.contains("DialogButtonType")) {
            line = line.replaceAll(Pattern.quote("DialogButtonType"), PREFIX +  "DialogButtonType");
        }
        if (line.contains("ButtonVisualState")) {
            line = line.replaceAll(Pattern.quote("ButtonVisualState"), PREFIX +  "ButtonVisualState");
        }

        if ((line.contains("class " + PREFIX + "ViewModel") || line.contains(PREFIX + "ViewModel,")) && !line.contains(PREFIX + "ComposeViewModel") && !line.contains(PREFIX + "ComposeViewModel")) {
            line = line.replaceAll(Pattern.quote(PREFIX + "ViewModel"), PREFIX + "ComposeViewModel");
        } else if (line.contains(PREFIX + "ViewModelCompose")) {
            line = line.replaceAll(Pattern.quote("ViewModelCompose"), "ComposeViewModel");
        }
        if (line.contains("private fun ")) {
            line = line.replaceAll(Pattern.quote("private fun "), "fun ");
        }
        if (line.contains("private data class ")) {
            line = line.replaceAll(Pattern.quote("private "), "");
        }
        if (line.contains("R.string.abrevZone")) {
            line = line.replaceAll(Pattern.quote("R.string.abrevZone"), "R.string.abrevUbZona");
        }
        if (line.contains("R.string.abrevStop")) {
            line = line.replaceAll(Pattern.quote("R.string.abrevStop"), "R.string.abrevUbParada");
        }
        if (line.contains(".zone.name")) {
            line = line.replaceAll(Pattern.quote(".zone.name"), ".zone.nombreZone");
        }
        if (line.contains("data class CustomDialogState")) {
            line = line.replaceAll(Pattern.quote("CustomDialogState"), PREFIX + "CustomDialogState");
        }
        if (line.contains("fun CustomDialog") && !line.contains(PREFIX + "CustomDialog")) {
            line = line.replaceAll(Pattern.quote("CustomDialog"), PREFIX + "CustomDialog");
        }
        if (line.contains(PREFIX + PREFIX + "CustomDialog")) {
            line = line.replaceAll(Pattern.quote(PREFIX + PREFIX), PREFIX);
        }
        if (printAlways) {
            if (line.contains("ifac.td.taxi.ui.screen.compose.dialog.")) {
                line = line.replaceAll(Pattern.quote("ifac.td.taxi.ui.screen.compose.dialog."), "ifac.td.taxi.ui.screen.state.");
            } else if (line.contains("ifac.td.taxi.ui.screen.compose.components.")) {
                line = line.replaceAll(Pattern.quote("ifac.td.taxi.ui.screen.compose.components."), "ifac.td.taxi.ui.screen.components.");
            } else if (line.contains("ifac.td.taxi.viewmodel.compose.")) {
                if (line.contains("ViewModel")) {
                    line = line.replaceAll(Pattern.quote("ifac.td.taxi.viewmodel.compose."), "ifac.td.taxi.compose.viewmodel.");
                } else {
                    line = line.replaceAll(Pattern.quote("ifac.td.taxi.viewmodel.compose."), "ifac.td.taxi.ui.screen.state.");
                }
            }
//            System.out.println(line);
            if (writer != null) {
                writer.println(line);
                writer.flush();
            }
        }
    }

    @Override
    public void begin() {
        super.begin(); 
        System.out.println("*** GenerarAnswerProcessor OPEN FILE " + FILE_NAME + " -> " + processedAnswerPath);
    }

    @Override
    public void end() {
        super.end();
//        System.out.println("*** END FILE " + FILE_NAME + " BLOCKS:");
//        for (Map.Entry<String, String> entry : blocks.entrySet()) {
//            System.out.println("    " + entry.getKey());
//        }
//        System.out.println("*** FILES:");
//        for (Map.Entry<String, String> entry : blockFiles.entrySet()) {
//            System.out.println("    " + entry.getKey() + " -> " + entry.getValue());
//        }
        for (String line : orderedLines.values()) {
            doProcessLine(line);
        }
        if (writer != null) {
            writer.close();
        }
        System.out.println("*** GenerarAnswerProcessor CLOSE FILE " + FILE_NAME + " lines = " + orderedLines.size() + " -> " + processedAnswerPath);
        if (orderedLines.isEmpty()) {
            System.out.println("======> GenerarAnswerProcessor CLOSE FILE " + FILE_NAME + " NO LINES!!!");
        }
        blocksList = new String[blocks.size()];
        int idx = 0;
        for (Map.Entry<String, String> entry : blocks.entrySet()) {
            blocksList[idx] = entry.getKey();
            idx++;
        }
        blockFilesList = new String[blockFiles.size()];
        idx = 0;
        for (Map.Entry<String, String> entry : blockFiles.entrySet()) {
            String path = "./generated-files/ui/screen/components/";
            String fileName = entry.getValue();
            if (fileName.contains("Screen.kt")) {
                path = "./generated-files/ui/screen/";
            } else if (fileName.contains("ViewModel.kt")) {
                path = "./generated-files/compose/viewmodel/";
            }
            blockFilesList[idx] = path + fileName;
            idx++;
        }
        idx = 0;
        packagesArray = new String[blockFiles.size()];
        for (Map.Entry<String, String> entry : blockFiles.entrySet()) {
            if (entry.getKey().contains("Screen.kt")) {
                packagesArray[idx] = "ifac.td.taxi.ui.screen";
            } else if (entry.getKey().contains("ViewModel.kt")) {
                packagesArray[idx] = "ifac.td.taxi.compose.viewmodel";
            } else {
                packagesArray[idx] = "ifac.td.taxi.ui.screen.components";
            }
            idx++;
        }
    }

    private String extractFileNameFromClass(String line) {
        return line.trim().replaceAll("data class ", "").replaceAll("class ", "").replaceAll(Pattern.quote("("), "").replaceAll(" ", "").trim() + ".kt";
    }

    private String extractFileNameFromFun(String line) {
// fun ContactCentralScreen(
        return line.replaceAll("fun ", "").replaceAll(Pattern.quote("("), "").replaceAll(" ", "").trim() + ".kt";
    }
    private String extractFileNameFromObject(String line) {
// object ComposeCustomButtonDefaults
        return line.replaceAll("object ", "").replaceAll(Pattern.quote("{"), "").replaceAll(" ", "").trim() + ".kt";
    }
}
