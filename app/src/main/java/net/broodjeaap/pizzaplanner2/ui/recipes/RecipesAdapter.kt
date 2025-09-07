package net.broodjeaap.pizzaplanner2.ui.recipes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import net.broodjeaap.pizzaplanner2.R
import net.broodjeaap.pizzaplanner2.data.models.Recipe
import net.broodjeaap.pizzaplanner2.databinding.ItemRecipeBinding
import net.broodjeaap.pizzaplanner2.utils.MarkdownUtils

class RecipesAdapter(
    private val onRecipeClick: (Recipe) -> Unit
) : ListAdapter<Recipe, RecipesAdapter.RecipeViewHolder>(RecipeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
        val binding = ItemRecipeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecipeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RecipeViewHolder(
        private val binding: ItemRecipeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(recipe: Recipe) {
            binding.apply {
                textViewRecipeName.text = recipe.name
                MarkdownUtils.setMarkdownText(textViewRecipeDescription, recipe.description)
                textViewDifficulty.text = recipe.difficulty
                textViewTotalTime.text = root.context.getString(
                    R.string.recipe_total_time,
                    recipe.totalTimeHours
                )

                // Set difficulty color
                val difficultyColor = when (recipe.difficulty.lowercase()) {
                    "easy" -> android.R.color.holo_green_dark
                    "medium" -> android.R.color.holo_orange_dark
                    "hard" -> android.R.color.holo_red_dark
                    else -> android.R.color.darker_gray
                }
                textViewDifficulty.setTextColor(
                    root.context.getColor(difficultyColor)
                )

                // Show variable count
                textViewVariables.text = "${recipe.variables.size} customizable variables"
                textViewSteps.text = "${recipe.steps.size} steps"
                
                // Load recipe image if available
                if (recipe.imageUrl != null && recipe.imageUrl.isNotEmpty()) {
                    imageViewRecipe.visibility = android.view.View.VISIBLE
                    Glide.with(root.context)
                        .load(recipe.imageUrl)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(imageViewRecipe)
                } else {
                    imageViewRecipe.visibility = android.view.View.GONE
                }

                root.setOnClickListener {
                    onRecipeClick(recipe)
                }
            }
        }
    }

    private class RecipeDiffCallback : DiffUtil.ItemCallback<Recipe>() {
        override fun areItemsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Recipe, newItem: Recipe): Boolean {
            return oldItem == newItem
        }
    }
}
