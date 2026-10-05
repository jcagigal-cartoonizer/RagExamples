package com.cartoonizer.rag.shared.utils;

import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.ONLY_THIS;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class GenerateNavigationFragment {

    public static void main(String[] args) {
        String outputPath = "./generated-files/compose/navigation";
        String prefix = ONLY_THIS;
        System.out.println("generateNavigationFragment " + outputPath + " prefix = " + prefix);
        String screenPath = "./generated-files/ui/screen/" + prefix + "Screen.kt";
        ExtractParametersFromScreen extractParametersFromScreen = new ExtractParametersFromScreen(screenPath, prefix);
        extractParametersFromScreen.load();
        System.out.println("*** ExtractParametersFromScreen numLiniesLlegides " + extractParametersFromScreen.numLiniesLlegides + " parameters " + extractParametersFromScreen.parameterNames.size());
        for (String value : extractParametersFromScreen.parameterNames.values()) {
            System.out.println("    -> " + value);
        }
        GenerateNavigationFragment generateNavigation = new GenerateNavigationFragment();
        generateNavigation.generateNavigationFragment(outputPath, prefix, extractParametersFromScreen.parameterNames);
    }

    public static class ExtractParametersFromScreen extends ReadFile {

        public String screenPath;
        public String prefix;
        public boolean constructorFound;
        public HashMap<String, String> parameterNames = new HashMap<>();

        public ExtractParametersFromScreen(String screenPath, String prefix) {
            super(screenPath);
            this.screenPath = screenPath;
            this.prefix = prefix;
            System.out.println("*** ExtractParametersFromScreen screenPath " + screenPath + " prefix " + prefix);
        }

        @Override
        public void processLine(String line) {
            if (line.trim().startsWith("fun " + prefix + "Screen(")) {
                constructorFound = true;
                System.out.println("*** ExtractParametersFromScreen constructorFound TRUE in " + line);
            }
            if (line.contains(")") && !line.contains("(") && !line.contains(":") && constructorFound) {
                constructorFound = false;
                System.out.println("*** ExtractParametersFromScreen set constructorFound FALSE in " + line);
            }
            if (constructorFound) {
                System.out.println("*** ExtractParametersFromScreen constructorFound line = " + line);
                if (!line.trim().startsWith("fun ")) {
                    if (!line.contains(")")) {
                        String param = line.trim();
                        int idx = param.indexOf(":");
                        if (idx > 0) {
                            param = param.substring(0, idx);
                        }
                        parameterNames.put(param, param);
                    } else if (line.contains(":")) {
                        int idx = line.indexOf(":");
                        if (idx > 0) {
                            String param = line.substring(0, idx).trim();
                            parameterNames.put(param, param);
                        }
                    }
                } else { // when ( and ) are in the same line: class ChangeDriverPinViewModel(context: Application) 
                    if (line.contains("(") && line.contains(")") && !line.contains(":")) {
                        int idx = line.indexOf("(");
                        String param = line.substring(idx + 1);
                        idx = param.indexOf(")");
                        if (idx > 0) {
                            param = param.substring(0, idx);
                            idx = param.indexOf(":");
                            if (idx > 0) {
                                param = param.substring(0, idx);
                            }
                        }
                        parameterNames.put(param, param);
                        constructorFound = false;
                    }
                }
            }
            if (line.contains(")") && !line.contains(":") && constructorFound) {
                constructorFound = false;
            }
        }
    }
    public static HashMap<String, String> parameterNames = new HashMap<>();

    public String getFragmentTemplate() {
        StringBuilder fragmentTemplate = new StringBuilder(
                """
            package ifac.td.taxi.compose.navigation
            import ifac.td.taxi.compose.viewmodel.__PREFIX__ComposeViewModel
            import ifac.td.taxi.ui.screen.__PREFIX__Screen
            import androidx.fragment.app.Fragment
            import org.koin.androidx.viewmodel.ext.android.viewModel
            import android.view.LayoutInflater
            import android.view.ViewGroup
            import android.os.Bundle
            import androidx.compose.ui.platform.ComposeView
            import android.view.View
            import androidx.fragment.app.FragmentManager
            import androidx.compose.ui.Modifier      
            import androidx.navigation.findNavController
            import org.koin.androidx.viewmodel.ext.android.viewModel
            class __PREFIX__NavigationFragment : Fragment() {
            
                private val viewModel: __PREFIX__ComposeViewModel by viewModel()
            
                override fun onCreateView(
                    inflater: LayoutInflater,
                    container: ViewGroup?,
                    savedInstanceState: Bundle?
                ): View {
                    return ComposeView(requireContext()).apply {
                        setContent {
                            __PREFIX__Screen(

                """
        );
        for (String value : parameterNames.values()) {
                    if (value.contains("viewModel")) {
                        fragmentTemplate.append(value).append(" = ").append("viewModel").append(",\n");
                    } else if (value.contains("modifier")) {
                        fragmentTemplate.append(value).append(" = ").append("Modifier").append(",\n");
                    } else if (value.contains("navController")) {
                        fragmentTemplate.append(value).append(" = ").append("findNavController()").append(",\n");
                    } else {
                        fragmentTemplate.append(value).append(" = ").append("{}").append(",\n");
                    }
        }
        fragmentTemplate.append("""   
                            )
                        }
                    }
                }
            }

            """);
        String template = fragmentTemplate.toString();
        return template;
    }

    public String generateNavigationFragment(String outputPath, String prefix, HashMap<String, String> parameterNames) {
        String fileToSave = outputPath + "/" + prefix + "NavigationFragment.kt";
        String content = getFragmentTemplate().replaceAll("__PREFIX__", prefix);
        String lines[] = content.split("\n");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.contains("return ") && line.contains("ComposeView(")) {
                String lineToAdd = "val uiState = viewModel.uiState.value";
                builder.append(lineToAdd).append("\n");
                lineToAdd = "val buttonsState = viewModel.uiState.buttonsState";
                builder.append(lineToAdd).append("\n");
                lineToAdd = "val dialogState = viewModel.uiState.dialogState";
                builder.append(lineToAdd).append("\n");
                
            }

            builder.append(line).append("\n");
            if (line.trim().startsWith(prefix + "Screen(")) {
                for (String value : parameterNames.values()) {
                    if (value.contains("viewModel")) {
                        builder.append(value).append(" = ").append("viewModel").append(",\n");
                    } else if (value.contains("modifier")) {
                        builder.append(value).append(" = ").append("Modifier").append(",\n");
                    } else if (value.contains("navController")) {
                        builder.append(value).append(" = ").append("findNavController()").append(",\n");
                    } else if (value.contains("uiState")) {
                        builder.append(value).append(" = ").append("uiState,\n");
                    } else if (value.contains("buttonsState")) {
                        builder.append(value).append(" = ").append("buttonsState,\n");
                    } else if (value.contains("dialogState")) {
                        builder.append(value).append(" = ").append("dialogState,\n");
                    } else if (value.contains("uiState")) {
                        builder.append(value).append(" = ").append("uiState,\n");
                    } else {
                        builder.append(value).append(" = ").append("{}").append(",\n");
                    }
                }
            }
            
            
        }
        try {
            String result = builder.toString();
            System.out.println("Generated file " + fileToSave);
            System.out.println("-> result =\n" + result);
//            System.out.println(content);
            Files.write(Paths.get(fileToSave), result.getBytes());
        } catch (Exception e) {
            System.out.println("EXCEPTION saving file " + fileToSave + ": " + e);
        }
        return content;
    }
}
