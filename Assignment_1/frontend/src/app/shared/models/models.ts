export type Role = 'VISITOR' | 'USER' | 'ADMIN';
export type MuscleGroup = 'CHEST' | 'BACK' | 'SHOULDERS' | 'ARMS' | 'LEGS' | 'CORE' | 'FULL_BODY' | 'CARDIO';
export type Difficulty = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
export type SortDir = 'asc' | 'desc';

export interface AuthResponse {
  token: string;
  type: string;
  userId: number;
  email: string;
  fullName: string;
  role: Role;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
}

export interface Workout {
  id: number;
  name: string;
  description: string;
  muscleGroup: MuscleGroup;
  difficulty: Difficulty;
  durationMinutes: number;
  caloriesBurned: number;
  createdAt: string;
  updatedAt: string;
  createdByName: string;
}

export interface WorkoutRequest {
  name: string;
  description: string;
  muscleGroup: MuscleGroup;
  difficulty: Difficulty;
  durationMinutes: number;
  caloriesBurned: number;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface WorkoutFilters {
  name?: string;
  muscleGroup?: MuscleGroup | '';
  difficulty?: Difficulty | '';
  minDuration?: number;
  maxDuration?: number;
  page: number;
  size: number;
  sortBy: string;
  sortDir: SortDir;
}

export interface User {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: Role;
  createdAt: string;
  enabled: boolean;
}

export interface DietPlan {
  goal: string;
  targetCalories: number;
  actualCalories: number;
  meals: DietMeal[];
  tips: string[];
  macroSplit: { protein: number; carbs: number; fat: number };
}

export interface DietMeal {
  mealType: string;
  name: string;
  calories: number;
  protein_g: number;
  carbs_g: number;
  fat_g: number;
}
