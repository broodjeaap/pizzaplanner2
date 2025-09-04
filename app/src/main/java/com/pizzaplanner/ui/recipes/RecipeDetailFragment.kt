package com.pizzaplanner.ui.recipes

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
import com.pizzaplanner.R
import com.pizzaplanner.data.models.Recipe
import com.pizzaplanner.databinding.FragmentRecipeDetailBinding
import com.pizzaplanner.utils.MarkdownUtils

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
        stepsAdapter = RecipeStepsAdapter()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
