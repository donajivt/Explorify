using System.Security.Claims;

public static class FakeUserHelper
{
    public static ClaimsPrincipal CreateUser(string userId = "test-user-123")
    {
        var claims = new List<Claim>
        {
            new Claim(ClaimTypes.NameIdentifier, userId)
        };

        var identity = new ClaimsIdentity(claims, "test");
        return new ClaimsPrincipal(identity);
    }
}
