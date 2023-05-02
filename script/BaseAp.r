# Load necessary libraries
library(foreign)
library(caret)
library(car)
library(nlme)
library(rms)
library(e1071)

# Load dataset
df <- Flow_Metrics_merged

# Print column names and summary statistics
cat("\nSummary statistics:\n")
print(summary(df))

# Print structure and number of rows of the dataset
cat("\nStructure of the dataset:\n")
str(df)
cat("\nNumber of rows:", nrow(df), "\n")

# Drop unnecessary columns
drop <- c("Post_release_defects")
df2 <- df[, !(names(df) %in% drop)]

# Remove rows with missing values
df2 <- na.omit(df2)

# Calculate correlation matrix
correlations <- cor(df2, method = "spearman")

# Find highly correlated columns to remove
highCorr <- findCorrelation(correlations, cutoff = 0.75)
low_correlation_data <- df2[, -highCorr]

# Perform redundancy analysis
redun_obj <- redun(~., df = low_correlation_data, nk = 0)
after_redun <- low_correlation_data[, !(names(low_correlation_data) %in% redun_obj$Out)]

df<-na.omit(df)

# Define formula for logistic regression model
form <- as.formula("Post_release_defects>0~lines_added+lines_deleted+Pre_release_defects+Number_of_Relying_on_Get_Cause_AP+Number_of_Throws_Generic_AP")

# Train the logistic regression model
new <- glm(formula = form, df = log10(after_redun + 1), family = binomial(link = "logit"))

# Print summary and R-squared of the model
cat("\nSummary of the model:\n")
print(summary(new))
cat("\nR-squared of the model:", 1 - new$deviance / new$null.deviance, "\n")

# Generate predictions for test df
testdata <- df.frame(lines_added = log10(mean(df$lines_added) + 1) * 1.1,
                       lines_deleted = log10(mean(df$lines_deleted) + 1) * 1.1,
                       Pre_release_defects = log10(mean(df$Pre_release_defects) + 1) * 1.1)
predictions <- predict(new, testdata, type = "response")

# Print the predictions and influence of each variable
cat("\nPredictions for test data:", predictions, "\n")
anova_result <- anova(new)
cat("\nInfluence of each variable:\n")
print(anova_result)
