package net.broodjeaap.pizzaplanner2.ui.active

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import net.broodjeaap.pizzaplanner2.R
import net.broodjeaap.pizzaplanner2.data.models.RecipeSubstep
import net.broodjeaap.pizzaplanner2.data.repository.PlannedRecipeRepository
import net.broodjeaap.pizzaplanner2.utils.MarkdownUtils
import java.util.HashMap

class SubstepsFragment : Fragment() {

    private val args: SubstepsFragmentArgs by navArgs()
    private lateinit var repository: PlannedRecipeRepository
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_substeps, container, false)
        
        // Set up the toolbar title
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.title = args.stepName
        
        // Set up back button
        toolbar.setNavigationOnClickListener {
            // Set result to indicate step completion for back button as well
            val result = Bundle().apply {
                putBoolean("stepCompleted", true)
            }
            parentFragmentManager.setFragmentResult("substepsResult", result)
            
            // Explicitly pop the back stack
            findNavController().popBackStack()
        }
        
        // Display substeps or step description
        val layoutSubstepsContainer = view.findViewById<LinearLayout>(R.id.layout_substeps_container)
        val stepDescription = view.findViewById<TextView>(R.id.textViewStepDescription)
        
        if (args.substeps.isNotEmpty()) {
            displaySubsteps(layoutSubstepsContainer, args.substeps.toList(), args.variableValues as Map<String, Double>)
            stepDescription.visibility = View.GONE
        } else {
            // Show step description when there are no substeps
            layoutSubstepsContainer.visibility = View.GONE
            stepDescription.visibility = View.VISIBLE
            // For steps without substeps, show a simple completion message
            val message = "Ready to complete: ${args.stepName}\n\nTap 'Mark Completed' when this step is finished."
            MarkdownUtils.setMarkdownText(stepDescription, message)
        }
        
        // Set up "Mark Completed" button
        val markCompletedButton = view.findViewById<Button>(R.id.button_mark_completed)
        markCompletedButton.setOnClickListener {
            // Set result to indicate that the step should be marked as completed
            val result = Bundle().apply {
                putBoolean("stepCompleted", true)
            }
            parentFragmentManager.setFragmentResult("substepsResult", result)
            
            // Explicitly pop the back stack
            findNavController().popBackStack()
        }
        
        return view
    }
    
    private fun displaySubsteps(container: LinearLayout, substeps: List<RecipeSubstep>, variableValues: Map<String, Double>) {
        container.removeAllViews()
        
        substeps.forEach { substep ->
            val substepView = createSubstepView(substep, variableValues)
            container.addView(substepView)
        }
    }
    
    private fun createSubstepView(substep: RecipeSubstep, variableValues: Map<String, Double>): View {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(
                resources.getDimensionPixelSize(R.dimen.substep_horizontal_padding),
                resources.getDimensionPixelSize(R.dimen.substep_vertical_padding),
                resources.getDimensionPixelSize(R.dimen.substep_horizontal_padding),
                resources.getDimensionPixelSize(R.dimen.substep_vertical_padding)
            )
        }
        
        // Substep name
        val nameTextView = TextView(context).apply {
            text = substep.name
            setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
            setTypeface(typeface, Typeface.BOLD)
        }
        layout.addView(nameTextView)
        
        // Substep description with markdown support
        val descriptionTextView = TextView(context).apply {
            setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodySmall)
        }
        
        // Process description with variable substitution
        var processedDescription = substep.description
        variableValues.forEach { (name, value) ->
            val placeholder = "{$name}"
            val displayValue = if (value == value.toInt().toDouble()) {
                value.toInt().toString()
            } else {
                String.format("%.1f", value)
            }
            processedDescription = processedDescription.replace(placeholder, displayValue)
        }
        
        // Set markdown text
        MarkdownUtils.setMarkdownText(descriptionTextView, processedDescription)
        
        layout.addView(descriptionTextView)
        
        return layout
    }
}