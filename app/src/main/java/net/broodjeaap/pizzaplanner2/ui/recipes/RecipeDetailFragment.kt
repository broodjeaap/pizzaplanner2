package net.broodjeaap.pizzaplanner2.ui.recipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import net.broodjeaap.pizzaplanner2.R
import net.broodjeaap.pizzaplanner2.data.models.Recipe
import net.broodjeaap.pizzaplanner2.data.models.RecipeStep
import net.broodjeaap.pizzaplanner2.data.models.StepTiming
import net.broodjeaap.pizzaplanner2.databinding.FragmentRecipeDetailBinding
import net.broodjeaap.pizzaplanner2.databinding.DialogStepDetailBinding
import net.broodjeaap.pizzaplanner2.utils.MarkdownUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RecipeDetailFragment : Fragment() {

    private var _binding: FragmentRecipeDetailBinding? = null
    private val binding get() = _binding!!
    
    private val args: RecipeDetailFragmentArgs by navArgs()
    private lateinit var variablesAdapter: RecipeVariablesAdapter
    private lateinit var stepsAdapter: RecipeStepsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val recipe = args.recipe
        setupUI(recipe)
        setupRecyclerViews(recipe)
        setupClickListeners(recipe)
    }

    private fun setupUI(recipe: Recipe) {
        binding.apply {
            textViewRecipeName.text = recipe.name
            MarkdownUtils.setMarkdownText(textViewRecipeDescription, recipe.description)
            textViewDifficulty.text = recipe.difficulty
            textViewTotalTime.text = getString(R.string.recipe_total_time, recipe.totalTimeHours)
            
            // Set difficulty color
            val difficultyColor = when (recipe.difficulty.lowercase()) {
                "easy" -> android.R.color.holo_green_dark
                "medium" -> android.R.color.holo_orange_dark
                "hard" -> android.R.color.holo_red_dark
                else -> android.R.color.darker_gray
            }
            textViewDifficulty.setTextColor(requireContext().getColor(difficultyColor))
            
            // Load recipe image if available
            if (recipe.imageUrl != null && recipe.imageUrl.isNotEmpty()) {
                imageViewRecipe.visibility = View.VISIBLE
                Glide.with(requireContext())
                    .load(recipe.imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageViewRecipe)
            } else {
                imageViewRecipe.visibility = View.GONE
            }
        }
    }

    private fun setupRecyclerViews(recipe: Recipe) {
        // Variables RecyclerView
        variablesAdapter = RecipeVariablesAdapter()
        binding.recyclerViewVariables.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = variablesAdapter
        }
        variablesAdapter.submitList(recipe.variables)

        // Steps RecyclerView
        stepsAdapter = RecipeStepsAdapter { step ->
            showStepDetailDialog(step)
        }
        binding.recyclerViewSteps.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = stepsAdapter
        }
        stepsAdapter.submitList(recipe.steps)
    }

    private fun setupClickListeners(recipe: Recipe) {
        binding.buttonPlanRecipe.setOnClickListener {
            // Navigate to planning fragment with recipe as argument
            val action = RecipeDetailFragmentDirections
                .actionRecipeDetailToPlanningWithRecipe(recipe)
            findNavController().navigate(action)
        }
        
        binding.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun showStepDetailDialog(step: RecipeStep) {
        val dialogBinding = DialogStepDetailBinding.inflate(layoutInflater)
        
        // Set step details
        dialogBinding.textViewStepTitle.text = step.name
        MarkdownUtils.setMarkdownText(dialogBinding.textViewStepDescription, step.description)
        
        // Set timing
        dialogBinding.textViewStepTiming.text = when (step.timing) {
            StepTiming.START -> getString(R.string.at_start)
            StepTiming.AFTER_PREVIOUS -> getString(R.string.after_previous_step)
            StepTiming.PARALLEL -> getString(R.string.in_parallel)
            StepTiming.SCHEDULED -> getString(R.string.scheduled_timing)
        }
        
        // Set duration
        step.durationMinutes?.let { duration ->
            dialogBinding.textViewStepDuration.text = when {
                duration < 60 -> getString(R.string.duration_minutes, duration)
                duration % 60 == 0 -> getString(R.string.duration_hours, duration / 60)
                else -> getString(R.string.duration_hours_minutes, duration / 60, duration % 60)
            }
            dialogBinding.textViewStepDurationLabel.visibility = View.VISIBLE
            dialogBinding.textViewStepDuration.visibility = View.VISIBLE
        } ?: run {
            dialogBinding.textViewStepDurationLabel.visibility = View.GONE
            dialogBinding.textViewStepDuration.visibility = View.GONE
        }
        
        // Set temperature
        step.temperature?.let { temperature ->
            dialogBinding.textViewStepTemperature.text = temperature
            dialogBinding.textViewStepTemperatureLabel.visibility = View.VISIBLE
            dialogBinding.textViewStepTemperature.visibility = View.VISIBLE
        } ?: run {
            dialogBinding.textViewStepTemperatureLabel.visibility = View.GONE
            dialogBinding.textViewStepTemperature.visibility = View.GONE
        }
        
        // Set notes
        step.notes?.let { notes ->
            MarkdownUtils.setMarkdownText(dialogBinding.textViewStepNotes, notes)
            dialogBinding.textViewStepNotesLabel.visibility = View.VISIBLE
            dialogBinding.textViewStepNotes.visibility = View.VISIBLE
        } ?: run {
            dialogBinding.textViewStepNotesLabel.visibility = View.GONE
            dialogBinding.textViewStepNotes.visibility = View.GONE
        }
        
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()
        
        dialogBinding.buttonClose.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
