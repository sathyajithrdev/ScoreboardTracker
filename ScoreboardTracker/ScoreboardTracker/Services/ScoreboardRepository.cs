using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Plugin.CloudFirestore;

using ScoreboardTracker.Common.Interfaces;
using ScoreboardTracker.Models;

namespace ScoreboardTracker.Services
{
    public class ScoreboardRepository : IScoreboardRepository
    {

        public async void AddDummyDataGroup()
        {
            await Task.Run(async () =>
                {
                    for (var i = 1; i <= 389; i++)
                    {
                        var game = new Game();
                        game.scores = new List<UserScore>();
                        //jith
                        game.scores.Add(new UserScore()
                        {
                            userId = "28bab11a-3ed7-4af0-b447-e81a9b471bd6",
                            scores = new List<int?>()
                                {i <= 134 ? 200 : 100, 100, 100, 100, 100, 100, i <= 135 ? 98 : 100}
                        });

                        //achan
                        game.scores.Add(new UserScore()
                        {
                            userId = "5b24a1df-f342-4119-92c3-da004d0d96af",
                            scores = new List<int?>()
                            {
                                i > 134 && i <= 279 ? 200 : 100, 100, 100, 100, 100, 100, i > 135 && i < 240 ? 98 : 100
                            }
                        });

                        //najan
                        game.scores.Add(new UserScore()
                        {
                            userId = "84cd8240-cf44-45f8-9277-3041f9dbde80",
                            scores = new List<int?>() { i > 279 ? 200 : 100, 100, 100, 100, 100, 100, i > 240 ? 98 : 100 }
                        });

                        var winner = game.scores.Aggregate((curMin, s) => curMin == null || (s.scores.Sum() ?? 0) <
                                                                          curMin.scores.Sum()
                            ? s
                            : curMin);

                        var looser = game.scores.Aggregate((curMax, s) => curMax == null || (s.scores.Sum() ?? 0) >
                                                                          curMax.scores.Sum()
                            ? s
                            : curMax);

                        game.winnerId = winner.userId;

                        game.looserId = looser.userId;
                        game.isCompleted = true;

                        await CrossCloudFirestore.Current
                            .Instance
                            .GetCollection("groups/VVmSk2oLEPAdPu9agt9T/games")
                            .AddDocumentAsync(game);

                        Thread.Sleep(400);

                    }
                }
             );
        }


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
            try
            {
                var usersQuery = await CrossCloudFirestore.Current
                    .Instance
                    .GetCollection("users")
                    .GetDocumentsAsync();

                return usersQuery?.ToObjects<User>()?.ToList();
            }
            catch (Exception ex)
            {
                Debug.WriteLine(ex.Message);
                return null;
            }


        }
    }
}
