package com.pizzaplanner.ui.recipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pizzaplanner.databinding.FragmentRecipesBinding
import com.pizzaplanner.utils.YamlParser

class RecipesFragment : Fragment() {

    private var _binding: FragmentRecipesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: RecipesViewModel
    private lateinit var recipesAdapter: RecipesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[RecipesViewModel::class.java]
        
        setupRecyclerView()
        observeViewModel()
        
        // Load recipes from assets
        loadRecipes()
    }

    private fun setupRecyclerView() {
        recipesAdapter = RecipesAdapter { recipe ->
            // Navigate to recipe detail fragment
            val action = RecipesFragmentDirections.actionRecipesToRecipeDetail(recipe)
            findNavController().navigate(action)
        }
        
        binding.recyclerViewRecipes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = recipesAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.recipes.observe(viewLifecycleOwner) { recipes ->
            recipesAdapter.submitList(recipes)
            
            // Show/hide empty state
            if (recipes.isEmpty()) {
                binding.recyclerViewRecipes.visibility = View.GONE
                binding.textViewEmpty.visibility = View.VISIBLE
            } else {
                binding.recyclerViewRecipes.visibility = View.VISIBLE
                binding.textViewEmpty.visibility = View.GONE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                // Show error message
                binding.textViewError.text = error
                binding.textViewError.visibility = View.VISIBLE
            } else {
                binding.textViewError.visibility = View.GONE
            }
        }
    }

    private fun loadRecipes() {
        try {
            val inputStream = requireContext().assets.open("recipes/pizza_recipes_converted.yaml")
            val yamlParser = YamlParser()
            val recipes = yamlParser.parseRecipes(inputStream)
            viewModel.setRecipes(recipes)
        } catch (e: Exception) {
            viewModel.setError("Failed to load recipes: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
