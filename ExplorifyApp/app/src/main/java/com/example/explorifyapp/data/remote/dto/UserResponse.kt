import com.example.explorifyapp.data.remote.model.User

data class UserResponse(
    val result: List<User>,
    val isSuccess: Boolean,
    val statusCode: Int
)