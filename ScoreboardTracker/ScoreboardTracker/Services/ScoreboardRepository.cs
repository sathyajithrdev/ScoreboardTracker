using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Plugin.CloudFirestore;

using ScoreboardTracker.Common.Interfaces;
using ScoreboardTracker.Models;

namespace ScoreboardTracker.Services
{
    public class ScoreboardRepository : IScoreboardRepository
    {
        public async Task<List<Group>> GetGroups()
        {
            var groupQuery = await CrossCloudFirestore.Current
                    .Instance
                    .GetCollection("groups")
                    .GetDocumentsAsync();

            return groupQuery?.ToObjects<Group>()?.ToList();
        }

        public async Task<Group> GetGroup()
        {
            var groupQuery = await CrossCloudFirestore.Current
                .Instance
                .GetCollection("groups")
                .GetDocumentsAsync();

            return groupQuery?.ToObjects<Group>()?.FirstOrDefault();
        }

        public async Task<List<Game>> GetGames(string groupId)
        {
            var gameDocQuery = await CrossCloudFirestore.Current
                .Instance
                .GetCollection($"groups/{groupId}/games")
                .GetDocumentsAsync();

            return gameDocQuery?.ToObjects<Game>()?.ToList();
        }

        public async Task<Game> GetOnGoingGame(string groupId)
        {
            var gameDocQuery = await CrossCloudFirestore.Current
                .Instance
                .GetCollection($"groups/{groupId}/games")
                .WhereEqualsTo("isCompleted", false)
                .GetDocumentsAsync();

            return gameDocQuery?.ToObjects<Game>()?.FirstOrDefault();
        }

        public async Task<bool> AddGame(string groupId, Game game)
        {
            await CrossCloudFirestore.Current
                .Instance
                .GetCollection($"groups/{groupId}/games")
                .AddDocumentAsync(game);

            return true;
        }

        public async Task<bool> UpdateGame(string groupId, Game game)
        {
            await CrossCloudFirestore.Current
                .Instance
                .GetCollection($"groups/{groupId}/games")
                .GetDocument(game.gameId)
                .UpdateDataAsync(game);

            return true;
        }

        public async Task<bool> DeleteGame(string groupId, string gameId)
        {
            await CrossCloudFirestore.Current
                .Instance
                .GetCollection($"groups/{groupId}/games")
                .GetDocument(gameId).DeleteDocumentAsync();

            return true;
        }

        public async Task<List<User>> GetAllUsers()
        {
            var usersQuery = await CrossCloudFirestore.Current
                .Instance
                .GetCollection("users")
                .GetDocumentsAsync();

            return usersQuery?.ToObjects<User>()?.ToList();
        }
    }
}
