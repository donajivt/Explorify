using Explorify.Api.Likes.Application.Interfaces;
using Moq;

public static class LikeServiceMock
{
    public static Mock<ILikeService> Get()
    {
        var mock = new Mock<ILikeService>();

        mock.Setup(s => s.UserHasLikedAsync(It.IsAny<string>(), It.IsAny<string>()))
            .ReturnsAsync(false);

      

        return mock;
    }
}
