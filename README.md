# MarriagePaper
MarriagePaper is a lightweight Paper plugin that adds roleplay-style marriages, adoptions and partner interactions to your server.

## Features
Propose, accept, reject and divorce players <br>
Adopt players into a married couple's family, accept/reject adoption <br>
Partner interactions: kiss, hug, teleport to partner, view partner inventory, gift items <br>
Children management (adopt, leave, pat) <br>
Persistent data stored in JSON files in the plugin data folder <br>
Sneak-right-click to kiss (on partners) <br>
Important: the plugin includes an silly adult/NSFW-styled command named /marry fuck. This can be controlled via permissions. <br>

## Quick install (server)
Download the MarriagePaper.jar from the newest release.
Place the jar in your server's plugins/directory.
Start or restart your server.
The plugin will create a data folder containing JSON files for stored data.

## Commands & usage 
All commands are under the main command alias /marry.

### /marry propose [player]
Description: Send a marriage proposal to an online player. <br>
Permission: marriage.marry <br>
Example: /marry propose Notch <br>

### /marry accept [player]
Description: Accept a marriage proposal from [player]. <br>
Permission: marriage.marry <br>
Example: /marry accept Notch <br>

### /marry reject [player] 
Description: Reject a marriage proposal from [player]. <br>
Permission: marriage.marry <br>
Example: /marry reject Notch <br>

### /marry divorce
Description: Divorce your partner (if married). <br>
Permission: marriage.marry <br>

### /marry adopt [player]
Description: Send an adoption request to [player] (must be married to adopt). <br>
Permission: marriage.adopt <br>
Example: /marry adopt Notch <br>

### /marry adopt accept [player]
Description: Accept an adoption request from [player]. <br>
Permission: marriage.adopt <br>
Example: /marry adopt accept Notch <br>

### /marry adopt reject [player]
Description: Reject an adoption request from [player]. <br>
Permission: marriage.adopt <br>
Example: /marry adopt reject Notch <br>

### /marry adopt leave
Description: Leave your adopted family (become an orphan again). <br>
Permission: marriage.adopt <br>
Example: /marry adopt leave <br>

### /marry adopt pat [child]
Description: Pat a child (only available to parents of the child). <br>
Permission: marriage.adopt.pat <br>
Example: /marry adopt pat Notch <br>

### /marry kiss
Description: Send a kiss action to your online partner (actionbar + particles). <br>
Permission: marriage.marry.kiss <br>
Additional: Also triggered when sneaking + right-clicking your partner. <br>

### /marry hug
Description: Hug your online partner (actionbar + particles). <br>
Permission: marriage.marry.hug <br>

### /marry tp
Description: Teleport to your partner with a small (3-second) delay and actionbar countdown. <br>
Permission: marriage.marry.tp <br>

### /marry inventory
Description: Open a read-only view of your partner's inventory (live-updating while open). <br>
Permission: marriage.marry.inventory <br>

### /marry gift
Description: Gift the item in your main hand to your partner (adds item to their inventory). <br>
Permission: marriage.marry.gift <br>

### /marry fuck
Description: Adult/roleplay command that triggers actionbars/particles and occasionally broadcasts a message. <br>
Permission: marriage.marry.fuck <br>
Note: Contains adult/NSFW language. Server admins should restrict access if undesired. <br>

### /marry modify relationship <straight|gay|lesbian>
Description: Change the relationship type for your marriage to one of the supported enums. <br>
Permission: marriage.marry <br>
Example: /marry modify relationship gay <br>

### /marry list
Description: List all marriages including children and days married. <br>
Permission: marriage.marry <br>
Example: /marry list <br>

## Credits
Authors: Beauver, Sebiann
