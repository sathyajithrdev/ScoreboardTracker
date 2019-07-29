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

        public async void AddDummyDataGroup(string groupId)
        {
            await Task.Run(async () =>
                {
                    for (var i = 1; i <= 135; i++)
                    {
                        var gameDocQuery = await CrossCloudFirestore.Current
                                                   .Instance
                                                   .GetCollection($"groups/{groupId}/games")
                                                   .WhereEqualsTo("isCompleted", true)
                                                   .GetDocumentsAsync();

                        List<Game> games = gameDocQuery?.ToObjects<Game>()?.ToList();

                        var toUpdate = games.Where(g => g.scores.Any(s => s.scores.Sum() == 800)).ToList();

                        toUpdate?.ForEach(g =>
                        {
                            g.scores.ForEach(s =>
                            {
                                s.scores.ForEach(u => u = u / 2);
                            });
                            g.scores = g.scores;
                            //await UpdateGame(groupId, g);
                            //Thread.Sleep(400);
                        });
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

        public async Task<List<Game>> GetCompletedGames(string groupId)
        {
            var gameDocQuery = await CrossCloudFirestore.Current
                .Instance
                .GetCollection($"groups/{groupId}/games")
                .WhereEqualsTo("isCompleted", true)
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

        public void StartGameScoreListner(string groupId, IGameScoreChangeListener listener)
        {
            CrossCloudFirestore.Current
                   .Instance
                   .GetCollection($"groups/{groupId}/games")
                   .WhereEqualsTo("isCompleted", false)
                   .AddSnapshotListener((snapshot, error) =>
                   {
                       if (snapshot != null)
                       {
                           if (snapshot.DocumentChanges.Any(d => d.Type == DocumentChangeType.Added))
                           {
                               listener.newGameCreated();
                           }
                           else if (snapshot.DocumentChanges.Any(d => d.Type == DocumentChangeType.Modified))
                           {
                               listener.gameScoreUpdated();
                           }
                       }
                   });
        }
    }
}
