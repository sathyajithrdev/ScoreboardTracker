using Plugin.CloudFirestore;
using ScoreboardTracker.Models;
using System;
using System.ComponentModel;
using System.Diagnostics;
using System.Linq;
using Xamarin.Forms;

namespace ScoreboardTracker.Views
{
    // Learn more about making custom code visible in the Xamarin.Forms previewer
    // by visiting https://aka.ms/xamarinforms-previewer
    [DesignTimeVisible(false)]
    public partial class MainPage : TabbedPage
    {
        public MainPage()
        {
            try
            {

                InitializeComponent();
                Xamarin.Forms.PlatformConfiguration.AndroidSpecific.TabbedPage.SetIsSwipePagingEnabled(this, false);
                getGroups();
            }
            catch (Exception ex)
            {
                Debug.WriteLine(ex.Message);
            }
        }

        private async void getGroups()
        {
            try
            {
                /*var documents =*/
                //var listUsers = new List<User>();
                //listUsers.Add(new User("Chandrajith", ""));
                //listUsers.Add(new User("Sathyajith", ""));

                //await CrossCloudFirestore.Current
                //        .Instance
                //        .GetCollection("users")
                //        .AddDocumentAsync(new User("Chandrajith", ""));

                //await CrossCloudFirestore.Current
                //       .Instance
                //       .GetCollection("users")
                //       .AddDocumentAsync(new User("Sathyajith", ""));

                //Group g = new Group();
                //g.groupName = "Leage of legends";
                //g.groupId = Guid.NewGuid().ToString();
                //g.userIds = new System.Collections.Generic.List<string>();
                //g.userIds.Add("FKJdwqAninfL1TvTPWNw");
                //g.userIds.Add("MIGVqZKqIzcZsAik8D81");
                //g.userIds.Add("k2JACjbt9q9kZ5yT5TDy");
                ////g.games = new System.Collections.Generic.List<Game>();


                //Game game = new Game();
                //game.gameId = Guid.NewGuid().ToString();                
                //game.addUserScore(new UserScore() { userId = "FKJdwqAninfL1TvTPWNw", scores = new System.Collections.Generic.List<int?> { 1, 2, 3, 4, 5, 6, 7 } });

                //g.games.Add(game);

                 //await CrossCloudFirestore.Current
                 //          .Instance
                 //          .GetCollection("groups")
                 //          .AddDocumentAsync(g);

                //await CrossCloudFirestore.Current
                //           .Instance
                //           .GetCollection($"groups/VVmSk2oLEPAdPu9agt9T/games")
                //           .AddDocumentAsync(game);

                //var count = documents.Count;

                //var groupQuery = await CrossCloudFirestore.Current
                //        .Instance
                //        .GetCollection("groups")
                //        .LimitTo(1)
                //        .GetDocumentsAsync();

                //var documentId = groupQuery.Documents.FirstOrDefault().Id;

                //Group group = groupQuery.ToObjects<Group>().FirstOrDefault();

                //await CrossCloudFirestore.Current
                //        .Instance
                //        .GetCollection("groups")
                //        .WhereEqualsTo(nameof(Group.groupName), group.groupName)
                //        .LimitTo(1)
                //        .G(group);
            }
            catch (Exception ex)
            {
                Debug.WriteLine(ex.Message);

            }

        }
    }
}