namespace Comentarios.API.DTOs;
public record ModerationRequest(string Text);

public class ModerationResponse
{
    public bool IsOffensive { get; init; }
    public string[] Flags { get; init; } = Array.Empty<string>();
    public double Score { get; init; }
    public string? Reason { get; init; }
}