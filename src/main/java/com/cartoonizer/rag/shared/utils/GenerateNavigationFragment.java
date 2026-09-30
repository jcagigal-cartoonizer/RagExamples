package com.cartoonizer.rag.shared.utils;

import static com.cartoonizer.rag.shared.utils.GeneralAnswerProcessor.ONLY_THIS;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GenerateNavigationFragment {
    public static void main(String[] args) {
        String outputPath = "./generated-files/compose/navigation";
        String prefix = ONLY_THIS;
        generateNavigationFragment(outputPath, prefix);
    }
    public static String FRAGMENT_TEMPLATE = 
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
            
            class __PREFIX__ComposeFragment : Fragment() {
            
                private val viewModel: __PREFIX__ComposeViewModel by viewModel()
            
                override fun onCreateView(
                    inflater: LayoutInflater,
                    container: ViewGroup?,
                    savedInstanceState: Bundle?
                ): View {
                    return ComposeView(requireContext()).apply {
                        setContent {
                            __PREFIX__Screen(
                                viewModel = viewModel,
                                onNavigateBack = { parentFragmentManager.popBackStack() }
                            )
                        }
                    }
                }
            }

            """;
    public static String generateNavigationFragment(String outputPath, String prefix) {
        String fileToSave = outputPath + "/" + prefix + "ComposeFragment.kt";
        String content = FRAGMENT_TEMPLATE.replaceAll("__PREFIX__", prefix);
        try {
            System.out.println("Generated file " + fileToSave);
            System.out.println(content);
            Files.write(Paths.get(fileToSave), content.getBytes());
        } catch (Exception e) {
            System.out.println("EXCEPTION saving file " + fileToSave + ": " + e);
        }
        return content;
    }
}
