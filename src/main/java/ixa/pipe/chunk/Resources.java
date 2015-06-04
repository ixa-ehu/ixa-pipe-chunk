/*
 * Copyright 2013 Rodrigo Agerri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */


package ixa.pipe.chunk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Resources {

  private InputStream chunkModel;


  public InputStream getChunkModelFromDir(String folder,String cmdOption) {

    if (cmdOption.equals("en")) {
        try {
            chunkModel = new FileInputStream(new File(folder+"en-chunk.bin"));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Resources.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    if (cmdOption.equals("es")) {
      chunkModel = getClass().getResourceAsStream(folder+"es-chunk.bin");
    }
    return chunkModel;
  }
  
  
  public InputStream getChunkModel(String cmdOption) {

    if (cmdOption.equals("en")) {
      chunkModel = getClass().getResourceAsStream(
          "/en-chunk.bin");
    }

    if (cmdOption.equals("es")) {
      chunkModel = getClass().getResourceAsStream(
          "/en-chunk.bin");
    }
    return chunkModel;
  }
}
